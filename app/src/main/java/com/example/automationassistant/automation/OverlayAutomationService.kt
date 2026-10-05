package com.example.automationassistant.automation

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.app.ServiceCompat
import com.example.automationassistant.R

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
    private lateinit var resolvers: WindowResolvers

    override fun onCreate() {
        super.onCreate()
        runCatching {
            resolvers = WindowResolvers(packageManager)
            homePackages = resolvers.homePackages()
            exemptPackages = resolvers.exemptPackages()
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
                isSystemPackage = resolvers::isSystemPackage,
                debounceFor = { foreground ->
                    TargetStore.strictnessFor(foreground).debounceMs
                },
            )
            serviceActive = true
            AutomationState.setRunning(true)
            TargetStore.markServiceEverEnabled()
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
        val preview = TargetStore.previewMode.value
        var handled = false
        if (!preview) {
            handled = performGlobalAction(GLOBAL_ACTION_BACK)
            if (!handled && serviceActive) {
                handled = performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
        activeEngine.onFired(SystemClock.uptimeMillis())
        when {
            preview -> {
                recordDismissal(
                    gamePackage = activeEngine.foregroundPackage,
                    overlayPackage = activeEngine.lastWindow?.packageName,
                    preview = true,
                )
                Log.i(TAG, "Preview: logged system layer over ${activeEngine.foregroundPackage}")
            }
            handled -> {
                AutomationState.recordBlocked()
                recordDismissal(
                    gamePackage = activeEngine.foregroundPackage,
                    overlayPackage = activeEngine.lastWindow?.packageName,
                )
                Log.i(TAG, "Dismissed system layer over ${activeEngine.foregroundPackage}")
            }
            else -> Log.w(TAG, "GLOBAL_ACTION_BACK was not handled")
        }
    }

    /**
     * Appends one entry to the local shield log — what popped up, over which
     * game, and when. Failures here must never affect the dismissal itself.
     */
    private fun recordDismissal(
        gamePackage: String?,
        overlayPackage: String?,
        preview: Boolean = false,
    ) {
        runCatching {
            ShieldLog.record(
                LogEntry(
                    atEpochMillis = System.currentTimeMillis(),
                    overlayPackage = overlayPackage.orEmpty(),
                    overlayLabel = overlayPackage?.let(resolvers::labelOf)
                        ?: getString(R.string.log_unknown_app),
                    gameLabel = gamePackage?.let(resolvers::labelOf)
                        ?: getString(R.string.log_unknown_app),
                    preview = preview,
                ),
            )
        }.onFailure { Log.w(TAG, "Failed to record dismissal", it) }
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

    // endregion

    // region Persistent foreground notification

    /**
     * Delegates to [ShieldNotification]; a notification hiccup is surfaced
     * as a friendly error instead of crashing the engine.
     */
    private fun startShieldNotification() {
        runCatching { ShieldNotification.startForeground(this) }.onFailure {
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
    }
}
