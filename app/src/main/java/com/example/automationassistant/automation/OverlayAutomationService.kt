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
    private var lastDispatchAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceActive = true
        AutomationState.setRunning(true)
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !serviceActive) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (!AutomationState.enabled.value) return

        val pkg = event.packageName?.toString() ?: return
        val cls = event.className?.toString()
        if (!OverlayRules.isTarget(pkg, cls, selfPackage = packageName)) return

        scheduleDismiss(pkg, cls)
    }

    private fun scheduleDismiss(pkg: String, cls: String?) {
        val now = SystemClock.uptimeMillis()
        if (!OverlayRules.shouldDispatch(now, lastDispatchAt, DEBOUNCE_MS)) return
        lastDispatchAt = now

        mainHandler.postDelayed({
            if (!serviceActive) return@postDelayed
            val handled = performGlobalAction(GLOBAL_ACTION_BACK) ||
                performGlobalAction(GLOBAL_ACTION_HOME)
            if (handled) {
                AutomationState.setLastDismissed(pkg)
                Log.i(TAG, "Dismissed window from $pkg (${cls ?: "unknown"})")
            }
        }, DEBOUNCE_MS)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        serviceActive = false
        AutomationState.setRunning(false)
        AutomationState.clearLastDismissed()
        super.onDestroy()
    }

    private companion object {
        private const val TAG = "OverlayAutomation"
        private const val DEBOUNCE_MS = 400L
    }
}
