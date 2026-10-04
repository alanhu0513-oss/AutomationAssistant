package com.example.automationassistant.automation

class OverlayEngine(
    private val selfPackage: String,
    private val debounceMs: Long = DEFAULT_DEBOUNCE_MS,
) {

    var lastWindow: WindowSnapshot? = null
        private set

    var foregroundPackage: String? = null
        private set

    var lastFiredAt: Long = 0L
        private set

    var scheduledAt: Long? = null
        private set

    private var firedOnce = false

    fun onEvent(event: WindowEvent, config: AutomationConfig): EngineAction {
        if (!OverlayRules.isTrackedEvent(event.type)) return EngineAction.NONE
        val packageName = event.packageName ?: return EngineAction.NONE
        lastWindow = WindowSnapshot(packageName, event.className)
        if (OverlayRules.shouldAdoptForeground(event.type, packageName, selfPackage, config.interrupterPackage)) {
            foregroundPackage = packageName
        }
        if (!OverlayRules.shouldTrigger(config, packageName, selfPackage, foregroundPackage)) {
            return EngineAction.NONE
        }
        return resolveDispatch(event.at)
    }

    fun onFired(now: Long) {
        firedOnce = true
        lastFiredAt = now
        scheduledAt = null
    }

    fun cancelScheduled() {
        scheduledAt = null
    }

    private fun resolveDispatch(now: Long): EngineAction {
        if (scheduledAt != null) return EngineAction.NONE
        if (firedOnce && !OverlayRules.shouldDispatch(now, lastFiredAt, debounceMs)) {
            val fireAt = lastFiredAt + debounceMs
            scheduledAt = fireAt
            return EngineAction.SCHEDULE
        }
        lastFiredAt = now
        firedOnce = true
        return EngineAction.FIRE
    }

    companion object {
        const val DEFAULT_DEBOUNCE_MS = 400L
    }
}
