package dev.aegis.shield.automation

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayEngineTest {

    private val self = "dev.aegis.shield"
    private val home = "com.vivo.launcher"
    private val dialer = "com.google.android.dialer"
    private val game = "com.example.game"
    private val protectedGames = setOf(game)

    private val systemPackages = setOf(
        "android",
        "com.android.systemui",
        "com.samsung.android.parentalcontrol",
    )

    private val appWindow = AccessibilityWindowInfo.TYPE_APPLICATION
    private val systemWindow = AccessibilityWindowInfo.TYPE_SYSTEM
    private val imeWindow = AccessibilityWindowInfo.TYPE_INPUT_METHOD

    private fun engine() = OverlayEngine(
        selfPackage = self,
        homePackages = setOf(home),
        exemptPackages = setOf(dialer),
        isSystemPackage = { it in systemPackages },
    )

    private fun event(
        pkg: String?,
        at: Long,
        windowType: Int? = null,
        type: Int = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
        cls: String? = "com.example.Activity",
    ) = WindowEvent(type = type, packageName = pkg, className = cls, windowType = windowType, at = at)

    private fun OverlayEngine.inGame(at: Long) =
        onEvent(event(pkg = game, at = at, windowType = appWindow), protectedGames)

    @Test
    fun `system dialog over a protected game fires immediately`() {
        val engine = engine()
        engine.inGame(at = 1_000L)
        assertEquals(game, engine.foregroundPackage)

        val action = engine.onEvent(
            event(pkg = "com.whatever.overlay", at = 2_000L, windowType = systemWindow),
            protectedGames,
        )
        assertEquals(EngineAction.FIRE, action)
        assertEquals(2_000L, engine.lastFiredAt)
        assertNull(engine.scheduledAt)
    }

    @Test
    fun `system-image takeover over a protected game fires`() {
        val engine = engine()
        engine.inGame(at = 1_000L)

        val action = engine.onEvent(
            event(pkg = "com.samsung.android.parentalcontrol", at = 2_000L, windowType = appWindow),
            protectedGames,
        )
        assertEquals(EngineAction.FIRE, action)
    }

    @Test
    fun `ordinary app window over a game never fires and becomes the foreground`() {
        val engine = engine()
        engine.inGame(at = 1_000L)

        val action = engine.onEvent(
            event(pkg = "com.browser", at = 2_000L, windowType = appWindow),
            protectedGames,
        )
        assertEquals(EngineAction.NONE, action)
        assertEquals("com.browser", engine.foregroundPackage)
    }

    @Test
    fun `leaving to the launcher stops protection`() {
        val engine = engine()
        engine.inGame(at = 1_000L)
        engine.onEvent(event(pkg = home, at = 2_000L, windowType = appWindow), protectedGames)
        assertEquals(home, engine.foregroundPackage)

        val action = engine.onEvent(
            event(pkg = "android", at = 3_000L, windowType = systemWindow),
            protectedGames,
        )
        assertEquals(EngineAction.NONE, action)
    }

    @Test
    fun `incoming call dialer is exempt from dismissal`() {
        val engine = engine()
        engine.inGame(at = 1_000L)

        val action = engine.onEvent(
            event(pkg = dialer, at = 2_000L, windowType = appWindow),
            protectedGames,
        )
        assertEquals(EngineAction.NONE, action)
    }

    @Test
    fun `keyboard never pauses protection and never fires`() {
        val engine = engine()
        engine.inGame(at = 1_000L)

        val imeAction = engine.onEvent(
            event(pkg = "com.keyboard.app", at = 2_000L, windowType = imeWindow),
            protectedGames,
        )
        assertEquals(EngineAction.NONE, imeAction)
        // Foreground stays with the game, so the next popup still gets caught.
        assertEquals(game, engine.foregroundPackage)

        val popAction = engine.onEvent(
            event(pkg = "com.android.systemui", at = 3_000L, windowType = systemWindow),
            protectedGames,
        )
        assertEquals(EngineAction.FIRE, popAction)
    }

    @Test
    fun `popup inside the debounce coalesces into one trailing fire`() {
        val engine = engine()
        engine.inGame(at = 1_000L)
        engine.onEvent(event(pkg = "android", at = 5_000L, windowType = systemWindow), protectedGames)

        val first = engine.onEvent(event(pkg = "android", at = 5_100L, windowType = systemWindow), protectedGames)
        assertEquals(EngineAction.SCHEDULE, first)
        assertEquals(5_400L, engine.scheduledAt)

        val second = engine.onEvent(event(pkg = "android", at = 5_200L, windowType = systemWindow), protectedGames)
        assertEquals(EngineAction.NONE, second)
        assertEquals(5_400L, engine.scheduledAt)
    }

    @Test
    fun `onFired clears the schedule and re-arms the debounce`() {
        val engine = engine()
        engine.inGame(at = 1_000L)
        engine.onEvent(event(pkg = "android", at = 5_000L, windowType = systemWindow), protectedGames)
        engine.onEvent(event(pkg = "android", at = 5_100L, windowType = systemWindow), protectedGames)
        assertEquals(5_400L, engine.scheduledAt)

        engine.onFired(5_400L)
        assertNull(engine.scheduledAt)
        assertEquals(5_400L, engine.lastFiredAt)

        val next = engine.onEvent(event(pkg = "android", at = 5_500L, windowType = systemWindow), protectedGames)
        assertEquals(EngineAction.SCHEDULE, next)
        assertEquals(5_800L, engine.scheduledAt)
    }

    @Test
    fun `popup after the debounce fires immediately again`() {
        val engine = engine()
        engine.inGame(at = 1_000L)
        engine.onEvent(event(pkg = "android", at = 5_000L, windowType = systemWindow), protectedGames)

        val action = engine.onEvent(event(pkg = "android", at = 5_401L, windowType = systemWindow), protectedGames)
        assertEquals(EngineAction.FIRE, action)
        assertEquals(5_401L, engine.lastFiredAt)
        assertNull(engine.scheduledAt)
    }

    @Test
    fun `cancelScheduled drops the pending trailing fire`() {
        val engine = engine()
        engine.inGame(at = 1_000L)
        engine.onEvent(event(pkg = "android", at = 5_000L, windowType = systemWindow), protectedGames)
        engine.onEvent(event(pkg = "android", at = 5_100L, windowType = systemWindow), protectedGames)
        assertEquals(5_400L, engine.scheduledAt)

        engine.cancelScheduled()
        assertNull(engine.scheduledAt)
        assertEquals(
            EngineAction.FIRE,
            engine.onEvent(event(pkg = "android", at = 5_900L, windowType = systemWindow), protectedGames),
        )
    }

    @Test
    fun `shouldDismissNow re-verifies against the latest foreground`() {
        val engine = engine()
        engine.inGame(at = 1_000L)
        engine.onEvent(event(pkg = "android", at = 2_000L, windowType = systemWindow), protectedGames)
        assertTrue(engine.shouldDismissNow(protectedGames))

        // The user switches away before a trailing fire runs.
        engine.onEvent(event(pkg = "com.browser", at = 2_100L, windowType = appWindow), protectedGames)
        assertFalse(engine.shouldDismissNow(protectedGames))
    }

    @Test
    fun `untracked and empty events are ignored`() {
        val engine = engine()
        val click = event(pkg = "android", at = 5_000L, type = AccessibilityEvent.TYPE_VIEW_CLICKED)
        assertEquals(EngineAction.NONE, engine.onEvent(click, protectedGames))
        assertEquals(EngineAction.NONE, engine.onEvent(event(pkg = null, at = 5_000L), protectedGames))
        assertNull(engine.lastWindow)
    }

    @Test
    fun `debounce is resolved per foreground game so strictness levels apply`() {
        val engine = OverlayEngine(
            selfPackage = self,
            homePackages = setOf(home),
            exemptPackages = setOf(dialer),
            isSystemPackage = { it in systemPackages },
            debounceFor = { pkg -> if (pkg == game) 1_000L else 400L },
        )
        engine.inGame(at = 1_000L)

        assertEquals(
            EngineAction.FIRE,
            engine.onEvent(event(pkg = "android", at = 5_000L, windowType = systemWindow), protectedGames),
        )

        // Gentle level: 1000 ms debounce → scheduled, not fired, at +100 ms.
        assertEquals(
            EngineAction.SCHEDULE,
            engine.onEvent(event(pkg = "android", at = 5_100L, windowType = systemWindow), protectedGames),
        )
        assertEquals(6_000L, engine.scheduledAt)
    }

    @Test
    fun `strict level re-arms after a very short debounce`() {
        val engine = OverlayEngine(
            selfPackage = self,
            homePackages = setOf(home),
            exemptPackages = setOf(dialer),
            isSystemPackage = { it in systemPackages },
            debounceFor = { 100L },
        )
        engine.inGame(at = 1_000L)
        engine.onEvent(event(pkg = "android", at = 5_000L, windowType = systemWindow), protectedGames)

        assertEquals(
            EngineAction.FIRE,
            engine.onEvent(event(pkg = "android", at = 5_101L, windowType = systemWindow), protectedGames),
        )
        assertEquals(5_101L, engine.lastFiredAt)
    }
}
