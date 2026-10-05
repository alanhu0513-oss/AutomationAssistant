package dev.aegis.shield.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun `unreachable feed fails soft instead of throwing`() = runBlocking {
        val result = UpdateChecker("http://127.0.0.1:9/latest").check("2.2.0")
        assertTrue("expected Failed, got $result", result is UpdateCheckResult.Failed)
    }

    @Test
    fun `malformed feed url fails soft instead of throwing`() = runBlocking {
        val result = UpdateChecker("not a url").check("2.2.0")
        assertTrue("expected Failed, got $result", result is UpdateCheckResult.Failed)
    }

    @Test
    fun `garbage response body fails soft`() {
        val result = ReleaseFeed.parseLatest("<html>offline captive portal</html>", "2.2.0")
        assertTrue("expected Failed, got $result", result is UpdateCheckResult.Failed)
    }
}
