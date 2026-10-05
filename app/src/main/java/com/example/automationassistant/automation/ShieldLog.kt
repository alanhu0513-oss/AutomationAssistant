package com.example.automationassistant.automation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * One dismissed popup, in the words a player would use: what popped up,
 * over which game, and when.
 */
data class LogEntry(
    val atEpochMillis: Long,
    val overlayPackage: String,
    val overlayLabel: String,
    val gameLabel: String,
    val preview: Boolean = false,
)

/**
 * Persistent, capped activity log of every popup the shield dismissed.
 *
 * Local-only by design — nothing leaves the device. Stored as a JSON array
 * through the same [KeyValueStore] seam as the rest of the app so it survives
 * process death, capped to [MAX_ENTRIES] so it can never grow unbounded.
 * The JSON is built with the kotlinx.serialization *tree* API (no codegen
 * plugin needed) and decoded leniently: one broken entry never erases the
 * rest of the log.
 */
object ShieldLog {

    const val KEY_SHIELD_LOG = "shield_log"
    const val MAX_ENTRIES = 100

    private val lock = Any()
    private var storage: KeyValueStore = InMemoryKeyValueStore()

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    fun hydrate(store: KeyValueStore) {
        synchronized(lock) {
            storage = store
            _entries.value = decode(store.readString(KEY_SHIELD_LOG))
        }
    }

    fun record(entry: LogEntry) {
        synchronized(lock) {
            val next = (listOf(entry) + _entries.value).take(MAX_ENTRIES)
            storage.writeString(KEY_SHIELD_LOG, encode(next))
            _entries.value = next
        }
    }

    fun clear() {
        synchronized(lock) {
            storage.writeString(KEY_SHIELD_LOG, null)
            _entries.value = emptyList()
        }
    }

    internal fun encode(entries: List<LogEntry>): String {
        val array: JsonArray = buildJsonArray {
            for (e in entries) {
                add(
                    buildJsonObject {
                        put("at", JsonPrimitive(e.atEpochMillis))
                        put("pkg", JsonPrimitive(e.overlayPackage))
                        put("lbl", JsonPrimitive(e.overlayLabel))
                        put("game", JsonPrimitive(e.gameLabel))
                        put("prev", JsonPrimitive(e.preview))
                    },
                )
            }
        }
        return array.toString()
    }

    internal fun decode(raw: String?): List<LogEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            Json.parseToJsonElement(raw).jsonArray.mapNotNull { element ->
                runCatching {
                    val obj: JsonObject = element.jsonObject
                    LogEntry(
                        atEpochMillis = obj["at"]?.jsonPrimitive?.longOrNull ?: return@runCatching null,
                        overlayPackage = obj["pkg"]?.jsonPrimitive?.content ?: "",
                        overlayLabel = obj["lbl"]?.jsonPrimitive?.content ?: "",
                        gameLabel = obj["game"]?.jsonPrimitive?.content ?: "",
                        preview = obj["prev"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false,
                    )
                }.getOrNull()
            }
        }.getOrElse { emptyList() }
    }
}
