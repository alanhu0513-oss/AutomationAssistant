package dev.aegis.shield.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateRulesTest {

    @Test
    fun `version name strips the tag prefix`() {
        assertEquals("1.1.0", UpdateRules.versionNameOf("v1.1.0"))
        assertEquals("2.0", UpdateRules.versionNameOf("V2.0"))
        assertEquals("1.0.1", UpdateRules.versionNameOf("1.0.1"))
    }

    @Test
    fun `parse version tolerates junk segments`() {
        assertEquals(listOf(1, 1, 0), UpdateRules.parseVersion("v1.1.0"))
        assertEquals(listOf(1, 1, 0), UpdateRules.parseVersion("1.1.0-rc1"))
        assertEquals(listOf(0), UpdateRules.parseVersion("garbage"))
    }

    @Test
    fun `newer release is detected`() {
        assertTrue(UpdateRules.isNewer("v1.1.0", "1.0.1"))
        assertTrue(UpdateRules.isNewer("v2.0.0", "1.9.9"))
        assertTrue(UpdateRules.isNewer("1.1.1", "1.1"))
    }

    @Test
    fun `equal or older release is not an update`() {
        assertFalse(UpdateRules.isNewer("v1.0.1", "1.0.1"))
        assertFalse(UpdateRules.isNewer("v1.0.0", "1.0.1"))
        assertFalse(UpdateRules.isNewer("1.1", "1.1.0"))
        assertFalse(UpdateRules.isNewer("garbage", "1.0.0"))
    }

    @Test
    fun `segments compare numerically not lexically`() {
        assertTrue(UpdateRules.isNewer("1.10.0", "1.9.0"))
        assertFalse(UpdateRules.isNewer("1.9.0", "1.10.0"))
    }
}
