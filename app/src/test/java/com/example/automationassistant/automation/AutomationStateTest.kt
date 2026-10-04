package com.example.automationassistant.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AutomationStateTest {

    @Before
    fun reset() {
        AutomationState.reset()
    }

    @Test
    fun `service reports inactive before it connects`() {
        assertFalse(AutomationState.isRunning.value)
    }

    @Test
    fun `blocked count starts at zero`() {
        assertEquals(0, AutomationState.blockedCount.value)
    }

    @Test
    fun `setRunning toggles the running flag`() {
        AutomationState.setRunning(true)
        assertTrue(AutomationState.isRunning.value)
        AutomationState.setRunning(false)
        assertFalse(AutomationState.isRunning.value)
    }

    @Test
    fun `recordBlocked increments the counter`() {
        AutomationState.recordBlocked()
        AutomationState.recordBlocked()
        AutomationState.recordBlocked()
        assertEquals(3, AutomationState.blockedCount.value)
    }

    @Test
    fun `publishing an error exposes it until cleared`() {
        assertNull(AutomationState.lastError.value)
        AutomationState.publishError("shield hit a snag")
        assertEquals("shield hit a snag", AutomationState.lastError.value)
        AutomationState.clearError()
        assertNull(AutomationState.lastError.value)
    }

    @Test
    fun `reset restores the initial state`() {
        AutomationState.setRunning(true)
        AutomationState.recordBlocked()
        AutomationState.publishError("boom")
        AutomationState.reset()
        assertFalse(AutomationState.isRunning.value)
        assertEquals(0, AutomationState.blockedCount.value)
        assertNull(AutomationState.lastError.value)
    }
}
