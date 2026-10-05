package dev.aegis.shield.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseFeedTest {

    private val currentVersion = "1.0.1"

    private fun feed(tag: String = "v1.1.0"): String =
        """{"tag_name":"$tag","html_url":"https://github.com/alanhu0513-oss/AutomationAssistant/releases/tag/$tag"}"""

    @Test
    fun `newer release is reported as available`() {
        val result = ReleaseFeed.parseLatest(feed(), currentVersion)
        assertTrue(result is UpdateCheckResult.Available)
        val info = (result as UpdateCheckResult.Available).info
        assertEquals("v1.1.0", info.tagName)
        assertEquals("1.1.0", info.versionName)
        assertEquals(
            "https://github.com/alanhu0513-oss/AutomationAssistant/releases/tag/v1.1.0",
            info.releaseUrl,
        )
    }

    @Test
    fun `same release is reported as up to date`() {
        assertEquals(
            UpdateCheckResult.UpToDate,
            ReleaseFeed.parseLatest(feed("v1.0.1"), currentVersion),
        )
    }

    @Test
    fun `older release is reported as up to date`() {
        assertEquals(
            UpdateCheckResult.UpToDate,
            ReleaseFeed.parseLatest(feed("v1.0.0"), currentVersion),
        )
    }

    @Test
    fun `malformed body fails`() {
        assertTrue(ReleaseFeed.parseLatest("not json", currentVersion) is UpdateCheckResult.Failed)
        assertTrue(ReleaseFeed.parseLatest("[]", currentVersion) is UpdateCheckResult.Failed)
    }

    @Test
    fun `missing fields fail`() {
        assertTrue(ReleaseFeed.parseLatest("""{"html_url":"x"}""", currentVersion) is UpdateCheckResult.Failed)
        assertTrue(ReleaseFeed.parseLatest("""{"tag_name":"v2.0.0"}""", currentVersion) is UpdateCheckResult.Failed)
    }

    @Test
    fun `null fields fail`() {
        assertTrue(
            ReleaseFeed.parseLatest("""{"tag_name":null,"html_url":"x"}""", currentVersion)
                is UpdateCheckResult.Failed,
        )
    }
}
