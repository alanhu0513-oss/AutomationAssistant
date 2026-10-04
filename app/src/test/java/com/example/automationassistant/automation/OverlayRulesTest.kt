package com.example.automationassistant.automation

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayRulesTest {

    private val self = "com.example.automationshield"
    private val home = "com.vivo.launcher"
    private val dialer = "com.google.android.dialer"
    private val game = "com.tencent.deltaforce"

    /** Runtime FLAG_SYSTEM resolver — fakes any OEM's preinstalled packages. */
    private val systemPackages = setOf(
        "android",
        "com.android.systemui",
        "com.samsung.android.parentalcontrol",
        "com.miui.securitycenter",
        "com.vivo.parentalcontrol",
        "com.google.android.apps.kids.familylink",
    )
    private val isSystem: (String) -> Boolean = { it in systemPackages }

    // region isSystemLevel

    @Test
    fun `system and accessibility-overlay window types are level system on any OEM`() {
        assertTrue(
            OverlayRules.isSystemLevel(AccessibilityWindowInfo.TYPE_SYSTEM, "com.whatever.app", isSystem),
        )
        assertTrue(
            OverlayRules.isSystemLevel(
                AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY,
                "com.whatever.app",
                isSystem,
            ),
        )
        // Even a user-installed package is a system layer when the window
        // itself is a TYPE_SYSTEM dialog — One UI, MIUI, Funtouch all do this.
        assertFalse(isSystem("com.whatever.app"))
    }

    @Test
    fun `transient layers are never system-level`() {
        assertFalse(
            OverlayRules.isSystemLevel(AccessibilityWindowInfo.TYPE_INPUT_METHOD, "com.keyboard.app", isSystem),
        )
        assertFalse(
            OverlayRules.isSystemLevel(
                AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER,
                "com.android.systemui",
                isSystem,
            ),
        )
    }

    @Test
    fun `application windows trust the runtime systemness check`() {
        // Parental-control activities from any brand ship in the system image.
        assertTrue(OverlayRules.isSystemLevel(AccessibilityWindowInfo.TYPE_APPLICATION, "com.samsung.android.parentalcontrol", isSystem))
        assertTrue(OverlayRules.isSystemLevel(AccessibilityWindowInfo.TYPE_APPLICATION, "com.miui.securitycenter", isSystem))
        assertTrue(OverlayRules.isSystemLevel(AccessibilityWindowInfo.TYPE_APPLICATION, "com.vivo.parentalcontrol", isSystem))
        // A user-installed app window is not a system layer.
        assertFalse(OverlayRules.isSystemLevel(AccessibilityWindowInfo.TYPE_APPLICATION, "com.browser", isSystem))
    }

    @Test
    fun `unresolved window type falls back to package systemness`() {
        assertTrue(OverlayRules.isSystemLevel(null, "android", isSystem))
        assertFalse(OverlayRules.isSystemLevel(null, "com.browser", isSystem))
        assertFalse(OverlayRules.isSystemLevel(AccessibilityWindowInfo.TYPE_APPLICATION, null, isSystem))
    }

    // endregion

    // region shouldAdoptForeground

    @Test
    fun `ordinary apps and the launcher become the foreground`() {
        assertTrue(
            OverlayRules.shouldAdoptForeground(
                packageName = game,
                selfPackage = self,
                homePackages = setOf(home),
                windowType = AccessibilityWindowInfo.TYPE_APPLICATION,
                isSystemPackage = isSystem,
            ),
        )
        assertTrue(
            OverlayRules.shouldAdoptForeground(
                packageName = home,
                selfPackage = self,
                homePackages = setOf(home),
                windowType = AccessibilityWindowInfo.TYPE_APPLICATION,
                isSystemPackage = isSystem,
            ),
        )
    }

    @Test
    fun `system layers, the keyboard and the own package never become the foreground`() {
        assertFalse(
            OverlayRules.shouldAdoptForeground(
                packageName = "com.android.systemui",
                selfPackage = self,
                homePackages = setOf(home),
                windowType = AccessibilityWindowInfo.TYPE_SYSTEM,
                isSystemPackage = isSystem,
            ),
        )
        assertFalse(
            OverlayRules.shouldAdoptForeground(
                packageName = "com.keyboard.app",
                selfPackage = self,
                homePackages = setOf(home),
                windowType = AccessibilityWindowInfo.TYPE_INPUT_METHOD,
                isSystemPackage = isSystem,
            ),
        )
        assertFalse(
            OverlayRules.shouldAdoptForeground(
                packageName = self,
                selfPackage = self,
                homePackages = setOf(home),
                windowType = AccessibilityWindowInfo.TYPE_APPLICATION,
                isSystemPackage = isSystem,
            ),
        )
        assertFalse(
            OverlayRules.shouldAdoptForeground(
                packageName = null,
                selfPackage = self,
                homePackages = setOf(home),
                windowType = null,
                isSystemPackage = isSystem,
            ),
        )
    }

    // endregion

    // region shouldDismiss

    @Test
    fun `a system dialog over a protected game is dismissed on every brand`() {
        // Signal A: the window itself is a TYPE_SYSTEM dialog (all OEMs).
        assertTrue(
            OverlayRules.shouldDismiss(
                foregroundPackage = game,
                eventPackage = "com.whatever.overlay",
                windowType = AccessibilityWindowInfo.TYPE_SYSTEM,
                protectedApps = setOf(game),
                selfPackage = self,
                homePackages = setOf(home),
                exemptPackages = setOf(dialer),
                isSystemPackage = isSystem,
            ),
        )
        // Signal B: a system-image parental-control activity takes over.
        assertTrue(
            OverlayRules.shouldDismiss(
                foregroundPackage = game,
                eventPackage = "com.samsung.android.parentalcontrol",
                windowType = AccessibilityWindowInfo.TYPE_APPLICATION,
                protectedApps = setOf(game),
                selfPackage = self,
                homePackages = setOf(home),
                exemptPackages = setOf(dialer),
                isSystemPackage = isSystem,
            ),
        )
        assertTrue(
            OverlayRules.shouldDismiss(
                foregroundPackage = game,
                eventPackage = "com.google.android.apps.kids.familylink",
                windowType = null,
                protectedApps = setOf(game),
                selfPackage = self,
                homePackages = setOf(home),
                exemptPackages = setOf(dialer),
                isSystemPackage = isSystem,
            ),
        )
    }

    @Test
    fun `no dismissal without a protected foreground`() {
        assertFalse(
            OverlayRules.shouldDismiss(
                foregroundPackage = null,
                eventPackage = "android",
                windowType = AccessibilityWindowInfo.TYPE_SYSTEM,
                protectedApps = setOf(game),
                selfPackage = self,
                homePackages = setOf(home),
                exemptPackages = setOf(dialer),
                isSystemPackage = isSystem,
            ),
        )
        assertFalse(
            OverlayRules.shouldDismiss(
                foregroundPackage = "com.browser",
                eventPackage = "android",
                windowType = AccessibilityWindowInfo.TYPE_SYSTEM,
                protectedApps = setOf(game),
                selfPackage = self,
                homePackages = setOf(home),
                exemptPackages = setOf(dialer),
                isSystemPackage = isSystem,
            ),
        )
        assertFalse(
            OverlayRules.shouldDismiss(
                foregroundPackage = game,
                eventPackage = "android",
                windowType = AccessibilityWindowInfo.TYPE_SYSTEM,
                protectedApps = emptySet(),
                selfPackage = self,
                homePackages = setOf(home),
                exemptPackages = setOf(dialer),
                isSystemPackage = isSystem,
            ),
        )
    }

    @Test
    fun `own package home dialer keyboard and ordinary apps are protected from dismissal`() {
        fun dismiss(eventPackage: String?, windowType: Int?) = OverlayRules.shouldDismiss(
            foregroundPackage = game,
            eventPackage = eventPackage,
            windowType = windowType,
            protectedApps = setOf(game),
            selfPackage = self,
            homePackages = setOf(home),
            exemptPackages = setOf(dialer),
            isSystemPackage = isSystem,
        )

        assertFalse(dismiss(game, AccessibilityWindowInfo.TYPE_APPLICATION))
        assertFalse(dismiss(self, AccessibilityWindowInfo.TYPE_SYSTEM))
        assertFalse(dismiss(home, AccessibilityWindowInfo.TYPE_APPLICATION))
        assertFalse(dismiss(dialer, AccessibilityWindowInfo.TYPE_APPLICATION))
        assertFalse(dismiss("com.keyboard.app", AccessibilityWindowInfo.TYPE_INPUT_METHOD))
        assertFalse(dismiss("com.browser", AccessibilityWindowInfo.TYPE_APPLICATION))
        assertFalse(dismiss(null, AccessibilityWindowInfo.TYPE_SYSTEM))
    }

    // endregion

    @Test
    fun `only window state changes are tracked`() {
        assertTrue(OverlayRules.isTrackedEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED))
        assertFalse(OverlayRules.isTrackedEvent(AccessibilityEvent.TYPE_WINDOWS_CHANGED))
        assertFalse(OverlayRules.isTrackedEvent(AccessibilityEvent.TYPE_VIEW_CLICKED))
    }

    @Test
    fun `dispatch allowed once the window has elapsed`() {
        assertTrue(OverlayRules.shouldDispatch(now = 1_000L, lastDispatchAt = 0L, windowMs = 400L))
        assertTrue(OverlayRules.shouldDispatch(now = 1_000L, lastDispatchAt = 600L, windowMs = 400L))
    }

    @Test
    fun `dispatch suppressed inside the debounce window`() {
        assertFalse(OverlayRules.shouldDispatch(now = 1_000L, lastDispatchAt = 800L, windowMs = 400L))
        assertFalse(OverlayRules.shouldDispatch(now = 1_000L, lastDispatchAt = 1_000L, windowMs = 400L))
    }
}
