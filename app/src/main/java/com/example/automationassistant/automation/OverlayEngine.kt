package com.example.automationassistant.automation

/**
 * Event-driven state machine that decides when to press BACK.
 *
 * Dispatch strategy: the very first trigger fires immediately (the popup is
 * already on screen and must go now); further triggers inside the debounce
 * window are coalesced into a single trailing fire, re-verified against the
 * current foreground before it runs.
 *
 * Everything brand-specific is injected at construction time as data —
 * resolved home/launcher packages, the default dialer, and a systemness
 * resolver — so this class itself is universal across every Android OEM.
 *
 * @param selfPackage this app — never adopted as foreground, never dismissed.
 * @param homePackages launcher package(s); adoptable, never dismissed.
 * @param exemptPackages packages that must survive (default dialer).
 * @param isSystemPackage runtime `FLAG_SYSTEM` resolver, already cached by the caller.
 */
class OverlayEngine(
    private val selfPackage: String,
    private val homePackages: Set<String>,
    private val exemptPackages: Set<String>,
    private val isSystemPackage: (String) -> Boolean,
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

    fun onEvent(event: WindowEvent, protectedApps: Set<String>): EngineAction {
        if (!OverlayRules.isTrackedEvent(event.type)) return EngineAction.NONE
        val packageName = event.packageName ?: return EngineAction.NONE
        lastWindow = WindowSnapshot(packageName, event.windowType, event.className)

        // Decide dismissal against the foreground captured BEFORE this event,
        // so the interrupting layer itself can never anchor a safe state.
        val shouldDismiss = OverlayRules.shouldDismiss(
            foregroundPackage = foregroundPackage,
            eventPackage = packageName,
            windowType = event.windowType,
            protectedApps = protectedApps,
            selfPackage = selfPackage,
            homePackages = homePackages,
            exemptPackages = exemptPackages,
            isSystemPackage = isSystemPackage,
        )

        if (OverlayRules.shouldAdoptForeground(
                packageName = packageName,
                selfPackage = selfPackage,
                homePackages = homePackages,
                windowType = event.windowType,
                isSystemPackage = isSystemPackage,
            )
        ) {
            foregroundPackage = packageName
        }

        if (!shouldDismiss) return EngineAction.NONE
        return resolveDispatch(event.at)
    }

    /**
     * Re-checks the pending dismissal against the latest known window — used
     * by the service right before pressing BACK so a stale trailing fire can
     * never escape after the user has already left the game.
     */
    fun shouldDismissNow(protectedApps: Set<String>): Boolean {
        val window = lastWindow ?: return false
        return OverlayRules.shouldDismiss(
            foregroundPackage = foregroundPackage,
            eventPackage = window.packageName,
            windowType = window.windowType,
            protectedApps = protectedApps,
            selfPackage = selfPackage,
            homePackages = homePackages,
            exemptPackages = exemptPackages,
            isSystemPackage = isSystemPackage,
        )
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
