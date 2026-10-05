package dev.aegis.shield.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShieldLogTest {

    private lateinit var store: InMemoryKeyValueStore

    @Before
    fun setUp() {
        store = InMemoryKeyValueStore()
        ShieldLog.hydrate(store)
    }

    private fun entry(
        at: Long = 1_000L,
        pkg: String = "com.android.systemui",
        label: String = "System UI",
        game: String = "Delta Force",
    ) = LogEntry(atEpochMillis = at, overlayPackage = pkg, overlayLabel = label, gameLabel = game)

    @Test
    fun `starts empty on a fresh store`() {
        assertEquals(emptyList<LogEntry>(), ShieldLog.entries.value)
    }

    @Test
    fun `record prepends the newest entry first`() {
        ShieldLog.record(entry(at = 1_000L))
        ShieldLog.record(entry(at = 2_000L))
        ShieldLog.record(entry(at = 3_000L))

        val entries = ShieldLog.entries.value
        assertEquals(3, entries.size)
        assertEquals(3_000L, entries[0].atEpochMillis)
        assertEquals(2_000L, entries[1].atEpochMillis)
        assertEquals(1_000L, entries[2].atEpochMillis)
    }

    @Test
    fun `record persists so a re-hydrate restores the log`() {
        ShieldLog.record(entry(at = 1_000L, label = "Parental Control"))

        ShieldLog.hydrate(store)

        assertEquals(1, ShieldLog.entries.value.size)
        assertEquals("Parental Control", ShieldLog.entries.value[0].overlayLabel)
    }

    @Test
    fun `log is capped at the maximum, dropping the oldest`() {
        repeat(ShieldLog.MAX_ENTRIES + 25) { i ->
            ShieldLog.record(entry(at = i.toLong()))
        }

        val entries = ShieldLog.entries.value
        assertEquals(ShieldLog.MAX_ENTRIES, entries.size)
        assertEquals((ShieldLog.MAX_ENTRIES + 24).toLong(), entries.first().atEpochMillis)
        assertEquals(25L, entries.last().atEpochMillis)
    }

    @Test
    fun `clear empties the log and the backing store`() {
        ShieldLog.record(entry())
        ShieldLog.clear()

        assertEquals(emptyList<LogEntry>(), ShieldLog.entries.value)
        ShieldLog.hydrate(store)
        assertEquals(emptyList<LogEntry>(), ShieldLog.entries.value)
    }

    @Test
    fun `encode decode round-trip preserves every field including json-hostile text`() {
        val original = listOf(
            entry(at = 42L, pkg = "com.x", label = "Quote \"and\" <angle> & \u00e9\u4e2d", game = "Delta Force"),
            entry(at = 43L, pkg = "", label = "", game = ""),
        )

        assertEquals(original, ShieldLog.decode(ShieldLog.encode(original)))
    }

    @Test
    fun `decode tolerates null blank and malformed payloads`() {
        assertEquals(emptyList<LogEntry>(), ShieldLog.decode(null))
        assertEquals(emptyList<LogEntry>(), ShieldLog.decode(""))
        assertEquals(emptyList<LogEntry>(), ShieldLog.decode("not json at all"))
        assertEquals(emptyList<LogEntry>(), ShieldLog.decode("{\"object\":\"instead of array\"}"))
    }

    @Test
    fun `decode keeps valid entries when one entry is broken`() {
        val raw = """
            [
              {"at":1,"pkg":"a","lbl":"A","game":"G"},
              {"at":"NaN","pkg":42},
              {"at":2,"pkg":"b","lbl":"B","game":"G"}
            ]
        """.trimIndent()

        val entries = ShieldLog.decode(raw)

        assertEquals(2, entries.size)
        assertEquals("A", entries[0].overlayLabel)
        assertEquals("B", entries[1].overlayLabel)
    }

    @Test
    fun `decode fills missing optional fields with safe defaults`() {
        val raw = """[{"at":7}]"""

        val entries = ShieldLog.decode(raw)

        assertEquals(1, entries.size)
        assertEquals(7L, entries[0].atEpochMillis)
        assertEquals("", entries[0].overlayPackage)
        assertEquals("", entries[0].overlayLabel)
        assertTrue(entries[0].gameLabel.isEmpty())
    }
}
