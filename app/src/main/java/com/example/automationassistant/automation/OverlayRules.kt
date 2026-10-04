package com.example.automationassistant.automation

import android.view.accessibility.AccessibilityEvent

data class WindowSnapshot(val packageName: String, val className: String?)

data class WindowEvent(
    val type: Int,
    val packageName: String?,
    val className: String?,
    val at: Long,
)

data class AutomationConfig(
    val enabled: Boolean,
    val interrupterPackage: String?,
    val targetProtectedApp: String?,
)

enum class EngineAction { NONE, FIRE, SCHEDULE }

object OverlayRules {

    fun isTrackedEvent(type: Int): Boolean =
        type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            type == AccessibilityEvent.TYPE_WINDOWS_CHANGED

    fun shouldAdoptForeground(
        type: Int,
        packageName: String?,
        selfPackage: String,
        interrupterPackage: String?,
    ): Boolean =
        type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            packageName != null &&
            packageName != selfPackage &&
            packageName != interrupterPackage

    fun shouldTrigger(
        config: AutomationConfig,
        packageName: String?,
        selfPackage: String,
        foregroundPackage: String?,
    ): Boolean {
        if (!config.enabled) return false
        val interrupter = config.interrupterPackage ?: return false
        if (packageName == null || packageName != interrupter || packageName == selfPackage) return false
        val protectedApp = config.targetProtectedApp ?: return true
        return foregroundPackage == protectedApp
    }

    fun shouldDispatch(now: Long, lastDispatchAt: Long, windowMs: Long): Boolean =
        now - lastDispatchAt >= windowMs
}
