package com.example.automationassistant.automation

/**
 * How aggressively the shield reacts over one specific game.
 *
 * The level only changes the engine's debounce window — the dismissal rules
 * themselves stay identical — so a "gentle" game still gets popups closed,
 * just with more coalescing and a calmer cadence.
 */
enum class Strictness(val key: String, val debounceMs: Long) {
    NORMAL("normal", 400L),
    GENTLE("gentle", 1_000L),
    STRICT("strict", 100L);

    fun next(): Strictness = when (this) {
        NORMAL -> GENTLE
        GENTLE -> STRICT
        STRICT -> NORMAL
    }

    companion object {
        /** Unknown or missing keys resolve to [NORMAL] — never throws. */
        fun fromKey(raw: String?): Strictness =
            entries.firstOrNull { it.key == raw } ?: NORMAL
    }
}
