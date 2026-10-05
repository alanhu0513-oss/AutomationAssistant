package dev.aegis.shield.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {

    private val apps = listOf(
        AppEntry("com.zeta.app", "Zeta"),
        AppEntry("com.alpha.app", "Alpha"),
        AppEntry("com.instagram.android", "Instagram"),
    )

    @Test
    fun `empty query returns everything in original order`() {
        assertEquals(apps, AppSearch.filter(apps, ""))
        assertEquals(apps, AppSearch.filter(apps, "   "))
    }

    @Test
    fun `label prefix matches rank first`() {
        val result = AppSearch.filter(apps, "alp")
        assertEquals(listOf("Alpha"), result.map { it.label })
    }

    @Test
    fun `matching is case insensitive`() {
        assertEquals(listOf("Alpha"), AppSearch.filter(apps, "ALP").map { it.label })
        assertEquals(listOf("Instagram"), AppSearch.filter(apps, "INSTA").map { it.label })
    }

    @Test
    fun `package names are searchable`() {
        val result = AppSearch.filter(apps, "instagram.android")
        assertEquals(listOf("Instagram"), result.map { it.label })
    }

    @Test
    fun `label substrings match`() {
        val result = AppSearch.filter(apps, "pha")
        assertEquals(listOf("Alpha"), result.map { it.label })
    }

    @Test
    fun `query is trimmed before matching`() {
        assertEquals(listOf("Alpha"), AppSearch.filter(apps, "  alpha  ").map { it.label })
    }

    @Test
    fun `no match returns an empty list`() {
        assertTrue(AppSearch.filter(apps, "does-not-exist").isEmpty())
    }

    @Test
    fun `prefix match wins over package match`() {
        val result = AppSearch.filter(apps, "a")
        assertEquals("Alpha", result.first().label)
        assertEquals(apps.size, result.size)
    }
}
