package com.example.automationassistant.automation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayRulesTest {

    private val self = "com.example.automationassistant"

    @Test
    fun `empty rule set never matches a package`() {
        assertFalse(OverlayRules.isTarget("com.example.blocker", "android.app.Dialog", self))
    }

    @Test
    fun `empty rule set never matches a class alone`() {
        assertFalse(OverlayRules.isTarget("com.other.app", "android.app.Dialog", self))
    }

    @Test
    fun `own package is never a target`() {
        assertFalse(OverlayRules.isTarget(self, "android.app.Dialog", self))
    }

    @Test
    fun `null class does not throw`() {
        assertFalse(OverlayRules.isTarget("com.example.blocker", null, self))
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

    @Test
    fun `targets ship empty by default`() {
        assertTrue(OverlayRules.targetPackages.isEmpty())
        assertTrue(OverlayRules.targetClasses.isEmpty())
        assertTrue(OverlayRules.targetCount == 0)
    }
}
