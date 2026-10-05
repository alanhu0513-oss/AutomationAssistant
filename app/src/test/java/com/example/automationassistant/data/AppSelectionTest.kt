package com.example.automationassistant.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSelectionTest {

    private val self = "com.example.automationassistant"

    private fun app(
        packageName: String,
        label: String? = null,
        isSystem: Boolean = false,
    ) = CandidateApp(
        packageName = packageName,
        label = label ?: packageName,
        isSystem = isSystem,
    )

    @Test
    fun `excludes this app itself`() {
        val selected = AppSelection.select(
            listOf(app(self), app("com.example.game")),
            selfPackage = self,
        )

        assertEquals(listOf("com.example.game"), selected.map { it.packageName })
    }

    @Test
    fun `excludes preinstalled system apps so the list stays clean`() {
        val selected = AppSelection.select(
            listOf(
                app("com.example.game"),
                app("com.android.chrome", isSystem = true),
                app("com.vendor.bloat", isSystem = true),
            ),
            selfPackage = self,
        )

        assertEquals(listOf("com.example.game"), selected.map { it.packageName })
    }

    @Test
    fun `blank labels fall back to the package name`() {
        val selected = AppSelection.select(
            listOf(app("com.example.game", label = "   ")),
            selfPackage = self,
        )

        assertEquals("com.example.game", selected.single().label)
    }

    @Test
    fun `duplicate packages keep only the first entry`() {
        val selected = AppSelection.select(
            listOf(
                app("com.example.game", label = "First"),
                app("com.example.game", label = "Second"),
            ),
            selfPackage = self,
        )

        assertEquals(listOf("First"), selected.map { it.label })
    }

    @Test
    fun `results are sorted case-insensitively by label`() {
        val selected = AppSelection.select(
            listOf(
                app("com.z", label = "alpha"),
                app("com.a", label = "Zeta"),
                app("com.b", label = "Beta"),
            ),
            selfPackage = self,
        )

        assertEquals(listOf("alpha", "Beta", "Zeta"), selected.map { it.label })
    }
}
