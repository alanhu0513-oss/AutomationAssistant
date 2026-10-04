package com.example.automationassistant.automation

import org.junit.Assert.assertFalse
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
}
