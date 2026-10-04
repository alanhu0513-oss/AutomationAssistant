package com.example.automationassistant.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OverlayAutomationService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var serviceActive = false
    private var lastDispatchAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceActive = true
        _isRunning.value = true
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !serviceActive) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (!_enabled.value) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        val cls = event.className?.toString()
        val matched = pkg in dismissTargetPackages || (cls != null && cls in dismissTargetClasses)
        if (!matched) return

        scheduleDismiss(pkg, cls)
    }

    private fun scheduleDismiss(pkg: String, cls: String?) {
        val now = SystemClock.uptimeMillis()
        if (now - lastDispatchAt < DEBOUNCE_MS) return
        lastDispatchAt = now

        mainHandler.postDelayed({
            if (!serviceActive) return@postDelayed
            val handled = performGlobalAction(GLOBAL_ACTION_BACK)
            if (!handled) performGlobalAction(GLOBAL_ACTION_HOME)
            if (handled) {
                _lastDismissed.value = pkg
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
        _isRunning.value = false
        _lastDismissed.value = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OverlayAutomation"
        private const val DEBOUNCE_MS = 400L

        private val dismissTargetPackages: Set<String> = emptySet()
        private val dismissTargetClasses: Set<String> = emptySet()

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _enabled = MutableStateFlow(true)
        val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

        private val _lastDismissed = MutableStateFlow<String?>(null)
        val lastDismissed: StateFlow<String?> = _lastDismissed.asStateFlow()

        val targetCount: Int
            get() = dismissTargetPackages.size + dismissTargetClasses.size

        fun setEnabled(value: Boolean) {
            _enabled.value = value
        }
    }
}
