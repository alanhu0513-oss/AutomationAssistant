package com.example.automationassistant.automation

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo

data class WindowSnapshot(
    val packageName: String,
    val windowType: Int?,
    val className: String?,
)

data class WindowEvent(
    val type: Int,
    val packageName: String?,
    val className: String?,
    /** [AccessibilityWindowInfo] type of the event's window, resolved live from the window hierarchy. */
    val windowType: Int?,
    val at: Long,
)

enum class EngineAction { NONE, FIRE, SCHEDULE }

/**
 * Pure decision logic for the overlay engine — completely brand agnostic.
 *
 * Instead of enumerating vendor packages (`com.vivo…`, `com.samsung…`,
 * `com.miui…`), interruption is decided from DYNAMIC signals that exist on
 * every Android device:
 *
 *  1. **Window type** — dialog/system windows surface as
 *     [AccessibilityWindowInfo.TYPE_SYSTEM] or [AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY]
 *     on AOSP, One UI, MIUI, ColorOS, Funtouch and everything else.
 *  2. **Package systemness** — a full-screen takeover from a preinstalled
 *     system image app (parental controls, digital wellbeing, OEM kid modes)
 *     is reported by `ApplicationInfo.FLAG_SYSTEM`, resolved at runtime.
 *
 * The only resolved-at-runtime exceptions are the user's own home launcher
 * and their default dialer — never hardcoded brand strings.
 */
object OverlayRules {

    fun isTrackedEvent(type: Int): Boolean =
        type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

    /**
     * True when the window [eventPackage] just presented counts as a
     * system-level layer on ANY brand of device.
     *
     * @param windowType dynamic type from the live window hierarchy; `null`
     *   when the hierarchy is unavailable (falls back to package systemness).
     * @param isSystemPackage runtime resolver: is the package part of the
     *   system image? Injected so unit tests can fake any OEM package.
     */
    fun isSystemLevel(
        windowType: Int?,
        eventPackage: String?,
        isSystemPackage: (String) -> Boolean,
    ): Boolean {
        if (eventPackage == null) return false
        return when (windowType) {
            // The universal, dynamic signal: system dialogs and overlays are
            // TYPE_SYSTEM / TYPE_ACCESSIBILITY_OVERLAY on every OEM.
            AccessibilityWindowInfo.TYPE_SYSTEM,
            AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY,
            -> true

            // Transient non-system layers are never interruptions.
            AccessibilityWindowInfo.TYPE_INPUT_METHOD,
            AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER,
            -> false

            // TYPE_APPLICATION (or unresolved): trust whether the window's
            // owner ships in the system image — parent-control and platform
            // dialogue activities all do, on every brand.
            else -> runCatching { isSystemPackage(eventPackage) }.getOrDefault(false)
        }
    }

    /**
     * Which events may re-anchor "what app is currently in front".
     *
     * Home is always adoptable (leaving the game stops protection), system
     * layers and the engine's own package never become the foreground, and a
     * transient IME/keyboard window must not pause protection mid-game.
     */
    fun shouldAdoptForeground(
        packageName: String?,
        selfPackage: String,
        homePackages: Set<String>,
        windowType: Int?,
        isSystemPackage: (String) -> Boolean,
    ): Boolean {
        val pkg = packageName ?: return false
        if (pkg == selfPackage) return false
        if (pkg in homePackages) return true
        if (windowType == AccessibilityWindowInfo.TYPE_INPUT_METHOD ||
            windowType == AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER
        ) {
            return false
        }
        return !isSystemLevel(windowType, pkg, isSystemPackage)
    }

    /**
     * The core trigger: press BACK when a system-level window appears
     * ([eventPackage] + [windowType]) while the user is inside one of their
     * [protectedApps] games.
     *
     * @param exemptPackages runtime-resolved packages that must never be
     *   dismissed (the default dialer, so incoming calls survive).
     */
    fun shouldDismiss(
        foregroundPackage: String?,
        eventPackage: String?,
        windowType: Int?,
        protectedApps: Set<String>,
        selfPackage: String,
        homePackages: Set<String>,
        exemptPackages: Set<String>,
        isSystemPackage: (String) -> Boolean,
    ): Boolean {
        val foreground = foregroundPackage ?: return false
        if (foreground !in protectedApps) return false
        val pkg = eventPackage ?: return false
        if (pkg == foreground || pkg == selfPackage) return false
        if (pkg in homePackages || pkg in exemptPackages) return false
        return isSystemLevel(windowType, pkg, isSystemPackage)
    }

    fun shouldDispatch(now: Long, lastDispatchAt: Long, windowMs: Long): Boolean =
        now - lastDispatchAt >= windowMs
}
