package dev.aegis.shield.automation

class OverlayEngine(
    private val selfPackage: String,
    private val homePackages: Set<String>,
    private val exemptPackages: Set<String>,
    private val isSystemVendorOverlay: (String) -> Boolean,
    private val debounceProvider: (String) -> Long
) {
    enum class Action {
        NONE,
        DISMISS,
        ADOPT_FOREGROUND
    }

    private var currentForegroundPackage: String? = null
    private var lastDismissTimestamp: Long = 0L

    fun evaluate(
        eventPackage: String,
        eventClass: String,
        timestamp: Long,
        protectedPackages: Set<String>
    ): Action {
        // 1. Ignore self events
        if (eventPackage == selfPackage) {
            return Action.NONE
        }

        // 2. Handle home screen launcher
        if (homePackages.contains(eventPackage)) {
            currentForegroundPackage = eventPackage
            return Action.ADOPT_FOREGROUND
        }

        // 3. Handle exempt packages (like incoming phone calls)
        if (exemptPackages.contains(eventPackage)) {
            return Action.NONE
        }

        // 4. Update foreground if it is a main application
        if (protectedPackages.contains(eventPackage)) {
            currentForegroundPackage = eventPackage
            return Action.ADOPT_FOREGROUND
        }

        // 5. If currently running a protected game and an overlay appears:
        val foreground = currentForegroundPackage
        if (foreground != null && protectedPackages.contains(foreground)) {
            val debounce = debounceProvider(foreground)
            if (timestamp - lastDismissTimestamp < debounce) {
                // Debounce threshold active
                return Action.NONE
            }

            // If it's a vendor popup/dialog/system overlay:
            if (isSystemVendorOverlay(eventPackage) || eventClass.contains("Dialog") || eventClass.contains("Alert")) {
                lastDismissTimestamp = timestamp
                return Action.DISMISS
            }
        }

        return Action.NONE
    }
}
