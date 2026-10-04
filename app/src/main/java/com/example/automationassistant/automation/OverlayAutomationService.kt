package com.example.automationassistant.automation

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.example.automationassistant.R
import java.util.concurrent.ConcurrentHashMap

/**
 * The only actor that touches the system: listens for window events, resolves
 * them against the LIVE window hierarchy, asks the [OverlayEngine] what to
 * do, and presses BACK when told to.
 *
 * Fully brand agnostic — no vendor package strings anywhere. System windows
 * are recognized by [AccessibilityWindowInfo] type and runtime
 * `ApplicationInfo.FLAG_SYSTEM` checks; home launcher and default dialer are
 * resolved dynamically at connect time.
 *
 * Runs as a `specialUse` foreground service with a persistent notification so
 * Android does not recycle the process during long sessions. Every entry
 * point is guarded: failures are logged and surfaced as a friendly snackbar
 * instead of crashing.
 */
class OverlayAutomationService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var serviceActive = false
    private var engine: OverlayEngine? = null
    private var homePackages: Set<String> = emptySet()
    private var exemptPackages: Set<String> = emptySet()

    /** pkg → FLAG_SYSTEM, resolved once per package. */
    private val systemnessCache = ConcurrentHashMap<String, Boolean>()

    override fun onCreate() {
        super.onCreate()
        runCatching {
            TargetStore.hydrate(this)
            homePackages = resolveHomePackages()
            exemptPackages = resolveExemptPackages()
            Log.i(TAG, "Resolved homes=$homePackages exempt=$exemptPackages")
        }.onFailure { Log.e(TAG, "Service init failed", it) }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        runCatching {
            engine = OverlayEngine(
                selfPackage = packageName,
                homePackages = homePackages,
                exemptPackages = exemptPackages,
                isSystemPackage = ::isSystemPackage,
            )
            serviceActive = true
            AutomationState.setRunning(true)
            startShieldNotification()
            Log.i(TAG, "Shield engine connected")
        }.onFailure {
            Log.e(TAG, "Failed to start shield engine", it)
            AutomationState.publishError(getString(R.string.error_engine))
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !serviceActive) return
        val activeEngine = engine ?: return
        try {
            val action = activeEngine.onEvent(
                WindowEvent(
                    type = event.eventType,
                    packageName = event.packageName?.toString(),
                    className = event.className?.toString(),
                    windowType = resolveWindowType(event.windowId),
                    at = SystemClock.uptimeMillis(),
                ),
                protectedApps = TargetStore.protectedApps.value,
            )
            when (action) {
                EngineAction.FIRE -> performDismiss()
                EngineAction.SCHEDULE -> scheduleTrailingFire()
                EngineAction.NONE -> Unit
            }
        } catch (t: Throwable) {
            // Never let an unexpected system error kill the accessibility stream.
            Log.e(TAG, "Error while processing accessibility event", t)
            AutomationState.publishError(getString(R.string.error_engine))
        }
    }

    private fun scheduleTrailingFire() {
        val activeEngine = engine ?: return
        val fireAt = activeEngine.scheduledAt ?: return
        val delay = (fireAt - SystemClock.uptimeMillis()).coerceAtLeast(0L)
        mainHandler.postDelayed(
            {
                if (!serviceActive) return@postDelayed
                // Re-verify right before acting: if the user has left the game
                // (or a new event changed the picture), drop the trailing fire.
                if (!activeEngine.shouldDismissNow(TargetStore.protectedApps.value)) {
                    activeEngine.cancelScheduled()
                    return@postDelayed
                }
                performDismiss()
            },
            delay,
        )
    }

    private fun performDismiss() {
        val activeEngine = engine ?: return
        if (!serviceActive) return
        if (!activeEngine.shouldDismissNow(TargetStore.protectedApps.value)) {
            activeEngine.cancelScheduled()
            return
        }
        var handled = performGlobalAction(GLOBAL_ACTION_BACK)
        if (!handled && serviceActive) {
            handled = performGlobalAction(GLOBAL_ACTION_BACK)
        }
        activeEngine.onFired(SystemClock.uptimeMillis())
        if (handled) {
            AutomationState.recordBlocked()
            Log.i(TAG, "Dismissed system layer over ${activeEngine.foregroundPackage}")
        } else {
            Log.w(TAG, "GLOBAL_ACTION_BACK was not handled")
        }
    }

    // region Dynamic, brand-agnostic resolvers

    /**
     * Resolves the window type of an event straight from the live window
     * hierarchy — this is how system dialogues are detected on ANY OEM
     * (One UI, MIUI, ColorOS, Funtouch…), without enumerating brands.
     * Returns `null` when the hierarchy is unavailable; the engine then falls
     * back to the runtime systemness check.
     */
    private fun resolveWindowType(windowId: Int): Int? {
        return try {
            val hierarchy: List<AccessibilityWindowInfo> = windows ?: return null
            hierarchy.firstOrNull { it.id == windowId }?.type
        } catch (t: Throwable) {
            Log.w(TAG, "Window hierarchy unavailable", t)
            null
        }
    }

    /** Runtime `FLAG_SYSTEM` check with a per-package cache — no brand lists. */
    private fun isSystemPackage(packageName: String): Boolean {
        systemnessCache[packageName]?.let { return it }
        val result = try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (t: Throwable) {
            Log.e(TAG, "Systemness check failed for $packageName", t)
            false
        }
        systemnessCache[packageName] = result
        return result
    }

    /** The launcher package(s); guards against dismissing the home screen. */
    private fun resolveHomePackages(): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return runCatching {
            packageManager.queryIntentActivities(homeIntent, 0)
                .mapNotNull { it.activityInfo?.packageName }
                .toSet()
        }.getOrElse { emptySet() }
    }

    /** The default dialer; an incoming call must never be dismissed. */
    private fun resolveExemptPackages(): Set<String> {
        return runCatching {
            val resolved = packageManager.resolveActivity(Intent(Intent.ACTION_DIAL), 0)
            setOfNotNull(resolved?.activityInfo?.packageName)
        }.getOrElse { emptySet() }
    }

    // endregion

    // region Persistent foreground notification

    /**
     * `specialUse` foreground service with a non-dismissible notification so
     * Android keeps the process alive across long matches. Declared in the
     * manifest with a PROPERTY_SPECIAL_USE_FGS_SUBTYPE justification.
     */
    private fun startShieldNotification() {
        runCatching {
            val manager = NotificationManagerCompat.from(this)
            val channel = NotificationChannelCompat
                .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MIN)
                .setName(getString(R.string.notif_channel_name))
                .setShowBadge(false)
                .build()
            manager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_shield)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text))
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()

            // specialUse is the correct type on Android 14+ (declared + justified
            // in the manifest); older platforms only know dataSync, which is
            // harmless there (no time limits before Android 15).
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            }
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                serviceType,
            )
            Log.i(TAG, "Shield notification active")
        }.onFailure {
            Log.e(TAG, "Could not start shield notification", it)
            // The engine still runs — surface a friendly notice instead of crashing.
            AutomationState.publishError(getString(R.string.error_engine))
        }
    }

    // endregion

    override fun onInterrupt() {
        Log.w(TAG, "Shield engine interrupted")
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        serviceActive = false
        engine = null
        runCatching { ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE) }
        AutomationState.setRunning(false)
        super.onDestroy()
    }

    private companion object {
        private const val TAG = "GamingShield"
        private const val CHANNEL_ID = "gaming_shield_active"
        private const val NOTIFICATION_ID = 4213
    }
}
