package com.example.automationassistant.automation

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class OverlayAutomationService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var serviceActive = false
    private var engine: OverlayEngine? = null

    override fun onCreate() {
        super.onCreate()
        TargetStore.hydrate(this)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        engine = OverlayEngine(selfPackage = packageName)
        serviceActive = true
        AutomationState.setRunning(true)
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !serviceActive) return
        val activeEngine = engine ?: return
        val config = AutomationConfig(
            enabled = AutomationState.enabled.value,
            interrupterPackage = TargetStore.interrupterPackageName.value,
            targetProtectedApp = TargetStore.targetProtectedApp.value,
        )
        val action = activeEngine.onEvent(
            WindowEvent(
                type = event.eventType,
                packageName = event.packageName?.toString(),
                className = event.className?.toString(),
                at = SystemClock.uptimeMillis(),
            ),
            config,
        )
        AutomationState.publishWindow(activeEngine.lastWindow, activeEngine.foregroundPackage)
        when (action) {
            EngineAction.FIRE -> performDismiss(config.interrupterPackage)
            EngineAction.SCHEDULE -> scheduleTrailingFire(config.interrupterPackage)
            EngineAction.NONE -> Unit
        }
    }

    private fun scheduleTrailingFire(interrupterPackage: String?) {
        val activeEngine = engine ?: return
        val fireAt = activeEngine.scheduledAt ?: return
        val delay = (fireAt - SystemClock.uptimeMillis()).coerceAtLeast(0L)
        mainHandler.postDelayed(
            {
                if (!serviceActive || interrupterPackage == null) return@postDelayed
                if (activeEngine.lastWindow?.packageName != interrupterPackage) {
                    activeEngine.cancelScheduled()
                    return@postDelayed
                }
                performDismiss(interrupterPackage)
            },
            delay,
        )
    }

    private fun performDismiss(interrupterPackage: String?) {
        val activeEngine = engine ?: return
        if (!serviceActive || interrupterPackage == null) return
        var handled = performGlobalAction(GLOBAL_ACTION_BACK)
        if (!handled && serviceActive) {
            handled = performGlobalAction(GLOBAL_ACTION_BACK)
        }
        activeEngine.onFired(SystemClock.uptimeMillis())
        if (handled) {
            AutomationState.setLastDismissed(interrupterPackage)
            Log.i(TAG, "Dismissed overlay from $interrupterPackage")
        } else {
            Log.w(TAG, "GLOBAL_ACTION_BACK was not handled for $interrupterPackage")
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        serviceActive = false
        engine = null
        AutomationState.setRunning(false)
        AutomationState.clearLastDismissed()
        AutomationState.clearWindows()
        super.onDestroy()
    }

    private companion object {
        private const val TAG = "OverlayAutomation"
    }
}
