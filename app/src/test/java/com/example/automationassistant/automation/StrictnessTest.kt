package com.example.automationassistant.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrictnessTest {

    @Test
    fun `debounce grows with gentleness and shrinks with strictness`() {
        assertTrue(Strictness.STRICT.debounceMs < Strictness.NORMAL.debounceMs)
        assertTrue(Strictness.NORMAL.debounceMs < Strictness.GENTLE.debounceMs)
    }

    @Test
    fun `normal is the default when nothing matches`() {
        assertEquals(Strictness.NORMAL, Strictness.fromKey(null))
        assertEquals(Strictness.NORMAL, Strictness.fromKey(""))
        assertEquals(Strictness.NORMAL, Strictness.fromKey("turbo"))
    }

    @Test
    fun `fromKey matches stored keys case-sensitively`() {
        assertEquals(Strictness.GENTLE, Strictness.fromKey("gentle"))
        assertEquals(Strictness.STRICT, Strictness.fromKey("strict"))
        assertEquals(Strictness.NORMAL, Strictness.fromKey("normal"))
        assertEquals(Strictness.NORMAL, Strictness.fromKey("GENTLE"))
    }

    @Test
    fun `next cycles through all three levels and wraps`() {
        val seen = mutableListOf<Strictness>()
        var level = Strictness.NORMAL
        repeat(3) {
            seen += level
            level = level.next()
        }

        assertEquals(
            listOf(Strictness.NORMAL, Strictness.GENTLE, Strictness.STRICT),
            seen,
        )
        assertEquals(Strictness.NORMAL, level)
    }
}
