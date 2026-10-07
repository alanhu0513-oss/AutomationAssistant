package dev.aegis.shield

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.app.NotificationCompat
import dev.aegis.shield.automation.OverlayEngine
import dev.aegis.shield.automation.TargetStore

class OverlayAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AegisAccessibility"
        private const val NOTIFICATION_ID = 1001
        var isRunning: Boolean = false
            private set
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var overlayEngine: OverlayEngine
    private lateinit var targetStore: TargetStore

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        targetStore = TargetStore(applicationContext)

        val homePackages = setOf(
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher",
            "com.mi.android.globallauncher",
            "com.sec.android.app.launcher",
            "com.oppo.launcher",
            "com.vivo.upslide",
            "com.huawei.android.launcher"
        )

        val exemptPackages = setOf(
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.incallui",
            "com.android.phone",
            "com.coloros.telephony",
            packageName
        )

        overlayEngine = OverlayEngine(
            selfPackage = packageName,
            homePackages = homePackages,
            exemptPackages = exemptPackages,
            isSystemVendorOverlay = { pkg ->
                pkg.startsWith("com.miui.") ||
                pkg.startsWith("com.vivo.") ||
                pkg.startsWith("com.coloros.") ||
                pkg.startsWith("com.samsung.android.") ||
                pkg == "com.android.packageinstaller" ||
                pkg == "com.google.android.permissioncontroller"
            },
            debounceProvider = { foregroundPkg ->
                targetStore.getDebounceMs(foregroundPkg)
            }
        )

        // Show persistent foreground notification
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        Log.i(TAG, "Aegis Shield Accessibility Engine Connected and Active")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""
        val eventType = event.eventType

        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {

            val protectedApps = targetStore.getProtectedApps()
            val now = System.currentTimeMillis()

            val action = overlayEngine.evaluate(
                eventPackage = pkgName,
                eventClass = className,
                timestamp = now,
                protectedPackages = protectedApps
            )

            if (action == OverlayEngine.Action.DISMISS) {
                if (targetStore.isPreviewMode()) {
                    Log.d(TAG, "[PREVIEW] Intercepted overlay from: $pkgName (No action taken)")
                } else {
                    Log.i(TAG, "[DISMISS] Auto-dismissing overlay from: $pkgName via GLOBAL_ACTION_BACK")
                    val success = performGlobalAction(GLOBAL_ACTION_BACK)
                    if (success) {
                        targetStore.incrementDismissalCount()
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Aegis Accessibility Engine Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.i(TAG, "Aegis Accessibility Engine Destroyed")
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, AegisApplication.CHANNEL_ID_PERSISTENCE)
            .setContentTitle("Aegis Shield Active")
            .setContentText("Foreground game windows protected against overlay popups")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
