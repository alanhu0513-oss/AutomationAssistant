package com.example.automationassistant.automation

import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayRulesTest {

    private val self = "com.example.automationassistant"
    private val interrupter = "com.example.interrupter"
    private val protectedApp = "com.example.protected"

    private fun config(
        enabled: Boolean = true,
        interrupterPackage: String? = interrupter,
        targetProtectedApp: String? = protectedApp,
    ) = AutomationConfig(
        enabled = enabled,
        interrupterPackage = interrupterPackage,
        targetProtectedApp = targetProtectedApp,
    )

    @Test
    fun `only window events are tracked`() {
        assertTrue(OverlayRules.isTrackedEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED))
        assertTrue(OverlayRules.isTrackedEvent(AccessibilityEvent.TYPE_WINDOWS_CHANGED))
        assertFalse(OverlayRules.isTrackedEvent(AccessibilityEvent.TYPE_VIEW_CLICKED))
        assertFalse(OverlayRules.isTrackedEvent(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED))
    }

    @Test
    fun `foreground adopts an ordinary state change`() {
        assertTrue(
            OverlayRules.shouldAdoptForeground(
                type = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                packageName = protectedApp,
                selfPackage = self,
                interrupterPackage = interrupter,
            ),
        )
    }

    @Test
    fun `foreground never adopts the interrupter overlay`() {
        assertFalse(
            OverlayRules.shouldAdoptForeground(
                type = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                packageName = interrupter,
                selfPackage = self,
                interrupterPackage = interrupter,
            ),
        )
    }

    @Test
    fun `foreground never adopts the own package`() {
        assertFalse(
            OverlayRules.shouldAdoptForeground(
                type = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                packageName = self,
                selfPackage = self,
                interrupterPackage = interrupter,
            ),
        )
    }

    @Test
    fun `foreground is not adopted from windows changed events`() {
        assertFalse(
            OverlayRules.shouldAdoptForeground(
                type = AccessibilityEvent.TYPE_WINDOWS_CHANGED,
                packageName = protectedApp,
                selfPackage = self,
                interrupterPackage = interrupter,
            ),
        )
    }

    @Test
    fun `foreground adoption tolerates a missing package`() {
        assertFalse(
            OverlayRules.shouldAdoptForeground(
                type = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                packageName = null,
                selfPackage = self,
                interrupterPackage = interrupter,
            ),
        )
    }

    @Test
    fun `trigger fires for the interrupter package`() {
        assertTrue(OverlayRules.shouldTrigger(config(), interrupter, self, protectedApp))
    }

    @Test
    fun `trigger requires a configured interrupter`() {
        assertFalse(
            OverlayRules.shouldTrigger(
                config(interrupterPackage = null),
                interrupter,
                self,
                protectedApp,
            ),
        )
    }

    @Test
    fun `trigger ignores other packages and null`() {
        assertFalse(OverlayRules.shouldTrigger(config(), "com.other.app", self, protectedApp))
        assertFalse(OverlayRules.shouldTrigger(config(), null, self, protectedApp))
    }

    @Test
    fun `own package never triggers`() {
        assertFalse(OverlayRules.shouldTrigger(config(), self, self, protectedApp))
    }

    @Test
    fun `trigger is gated off while disabled`() {
        assertFalse(
            OverlayRules.shouldTrigger(
                config(enabled = false),
                interrupter,
                self,
                protectedApp,
            ),
        )
    }

    @Test
    fun `trigger guarded by the protected app foreground`() {
        assertFalse(OverlayRules.shouldTrigger(config(), interrupter, self, "com.browser"))
        assertFalse(OverlayRules.shouldTrigger(config(), interrupter, self, null))
    }

    @Test
    fun `guard is skipped when no protected app is configured`() {
        assertTrue(
            OverlayRules.shouldTrigger(
                config(targetProtectedApp = null),
                interrupter,
                self,
                null,
            ),
        )
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
