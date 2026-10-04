package com.example.automationassistant.automation

import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OverlayEngineTest {

    private val self = "com.example.automationassistant"
    private val interrupter = "com.example.interrupter"
    private val protectedApp = "com.example.protected"

    private val guardedConfig = AutomationConfig(
        enabled = true,
        interrupterPackage = interrupter,
        targetProtectedApp = protectedApp,
    )

    private val openConfig = AutomationConfig(
        enabled = true,
        interrupterPackage = interrupter,
        targetProtectedApp = null,
    )

    private fun event(
        pkg: String?,
        at: Long,
        type: Int = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
        cls: String? = "com.example.Activity",
    ) = WindowEvent(type = type, packageName = pkg, className = cls, at = at)

    @Test
    fun `first trigger fires immediately`() {
        val engine = OverlayEngine(self)
        val action = engine.onEvent(event(pkg = interrupter, at = 5_000L), openConfig)
        assertEquals(EngineAction.FIRE, action)
        assertEquals(5_000L, engine.lastFiredAt)
        assertNull(engine.scheduledAt)
    }

    @Test
    fun `trigger inside the debounce schedules one coalesced trailing fire`() {
        val engine = OverlayEngine(self)
        engine.onEvent(event(pkg = interrupter, at = 5_000L), openConfig)

        val first = engine.onEvent(event(pkg = interrupter, at = 5_100L), openConfig)
        assertEquals(EngineAction.SCHEDULE, first)
        assertEquals(5_400L, engine.scheduledAt)

        val second = engine.onEvent(event(pkg = interrupter, at = 5_200L), openConfig)
        assertEquals(EngineAction.NONE, second)
        assertEquals(5_400L, engine.scheduledAt)
    }

    @Test
    fun `onFired clears the schedule and re-arms the debounce`() {
        val engine = OverlayEngine(self)
        engine.onEvent(event(pkg = interrupter, at = 5_000L), openConfig)
        engine.onEvent(event(pkg = interrupter, at = 5_100L), openConfig)
        assertEquals(5_400L, engine.scheduledAt)

        engine.onFired(5_400L)
        assertNull(engine.scheduledAt)
        assertEquals(5_400L, engine.lastFiredAt)
        assertEquals(EngineAction.SCHEDULE, engine.onEvent(event(pkg = interrupter, at = 5_500L), openConfig))
        assertEquals(5_800L, engine.scheduledAt)
    }

    @Test
    fun `trigger after the debounce fires immediately again`() {
        val engine = OverlayEngine(self)
        engine.onEvent(event(pkg = interrupter, at = 5_000L), openConfig)
        val action = engine.onEvent(event(pkg = interrupter, at = 5_401L), openConfig)
        assertEquals(EngineAction.FIRE, action)
        assertEquals(5_401L, engine.lastFiredAt)
        assertNull(engine.scheduledAt)
    }

    @Test
    fun `cancelScheduled drops the pending trailing fire`() {
        val engine = OverlayEngine(self)
        engine.onEvent(event(pkg = interrupter, at = 5_000L), openConfig)
        engine.onEvent(event(pkg = interrupter, at = 5_100L), openConfig)
        assertEquals(5_400L, engine.scheduledAt)

        engine.cancelScheduled()
        assertNull(engine.scheduledAt)
        assertEquals(EngineAction.FIRE, engine.onEvent(event(pkg = interrupter, at = 5_900L), openConfig))
    }

    @Test
    fun `interrupter fires only while the protected app is foreground`() {
        val engine = OverlayEngine(self)
        engine.onEvent(event(pkg = protectedApp, at = 1_000L), guardedConfig)
        assertEquals(protectedApp, engine.foregroundPackage)

        val action = engine.onEvent(event(pkg = interrupter, at = 2_000L), guardedConfig)
        assertEquals(EngineAction.FIRE, action)
    }

    @Test
    fun `interrupter over a foreign foreground does not fire`() {
        val engine = OverlayEngine(self)
        engine.onEvent(event(pkg = "com.browser", at = 1_000L), guardedConfig)

        val action = engine.onEvent(event(pkg = interrupter, at = 2_000L), guardedConfig)
        assertEquals(EngineAction.NONE, action)
    }

    @Test
    fun `interrupter never becomes the foreground`() {
        val engine = OverlayEngine(self)
        engine.onEvent(event(pkg = interrupter, at = 1_000L), openConfig)
        assertNull(engine.foregroundPackage)
        assertEquals(interrupter, engine.lastWindow?.packageName)
    }

    @Test
    fun `windows changed events update diagnostics without adopting foreground`() {
        val engine = OverlayEngine(self)
        engine.onEvent(event(pkg = protectedApp, at = 1_000L), guardedConfig)
        engine.onEvent(
            event(pkg = "com.other", at = 1_500L, type = AccessibilityEvent.TYPE_WINDOWS_CHANGED),
            guardedConfig,
        )
        assertEquals(protectedApp, engine.foregroundPackage)
        assertEquals("com.other", engine.lastWindow?.packageName)
    }

    @Test
    fun `disabled config never fires`() {
        val engine = OverlayEngine(self)
        val disabled = openConfig.copy(enabled = false)
        assertEquals(EngineAction.NONE, engine.onEvent(event(pkg = interrupter, at = 5_000L), disabled))
        assertEquals(0L, engine.lastFiredAt)
    }

    @Test
    fun `untracked and empty events are ignored`() {
        val engine = OverlayEngine(self)
        val click = event(pkg = interrupter, at = 5_000L, type = AccessibilityEvent.TYPE_VIEW_CLICKED)
        assertEquals(EngineAction.NONE, engine.onEvent(click, openConfig))
        assertEquals(EngineAction.NONE, engine.onEvent(event(pkg = null, at = 5_000L), openConfig))
        assertNull(engine.lastWindow)
    }
}
