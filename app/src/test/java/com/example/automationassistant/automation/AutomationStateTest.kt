package com.example.automationassistant.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationStateTest {

    @Test
    fun `service reports inactive before it connects`() {
        assertFalse(AutomationState.isRunning.value)
    }

    @Test
    fun `dismiss actions are enabled by default`() {
        assertTrue(AutomationState.enabled.value)
    }

    @Test
    fun `setEnabled toggles the gate`() {
        AutomationState.setEnabled(false)
        assertFalse(AutomationState.enabled.value)
        AutomationState.setEnabled(true)
        assertTrue(AutomationState.enabled.value)
    }

    @Test
    fun `publishing a window exposes diagnostics`() {
        val window = WindowSnapshot("com.example.interrupter", "com.example.Overlay")
        AutomationState.publishWindow(window, "com.example.protected")
        assertEquals(window, AutomationState.lastWindow.value)
        assertEquals("com.example.protected", AutomationState.foregroundPackage.value)
    }

    @Test
    fun `clearing windows resets diagnostics`() {
        AutomationState.publishWindow(WindowSnapshot("com.a", null), "com.b")
        AutomationState.clearWindows()
        assertNull(AutomationState.lastWindow.value)
        assertNull(AutomationState.foregroundPackage.value)
    }

    @Test
    fun `clearing the last dismissal empties it`() {
        AutomationState.setLastDismissed("com.example.interrupter")
        AutomationState.clearLastDismissed()
        assertNull(AutomationState.lastDismissed.value)
    }
}
