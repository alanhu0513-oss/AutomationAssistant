package com.example.automationassistant.automation

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Minimal persistence seam. Everything the UI or the service needs at runtime
 * is a string, a string set or a boolean — keep it that way.
 */
interface KeyValueStore {
    fun readString(key: String): String?
    fun writeString(key: String, value: String?)
    fun readStringSet(key: String): Set<String>
    fun writeStringSet(key: String, value: Set<String>)
    fun readBoolean(key: String, default: Boolean): Boolean
    fun writeBoolean(key: String, value: Boolean)
}

/** Test double backed by plain maps. */
class InMemoryKeyValueStore : KeyValueStore {
    private val strings = mutableMapOf<String, String>()
    private val stringSets = mutableMapOf<String, Set<String>>()
    private val booleans = mutableMapOf<String, Boolean>()

    override fun readString(key: String): String? = strings[key]

    override fun writeString(key: String, value: String?) {
        if (value == null) strings.remove(key) else strings[key] = value
    }

    override fun readStringSet(key: String): Set<String> = stringSets[key] ?: emptySet()

    override fun writeStringSet(key: String, value: Set<String>) {
        stringSets[key] = value
    }

    override fun readBoolean(key: String, default: Boolean): Boolean = booleans[key] ?: default

    override fun writeBoolean(key: String, value: Boolean) {
        booleans[key] = value
    }
}

/** Production store backed by SharedPreferences. */
class SharedPreferencesStore(context: Context) : KeyValueStore {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(TargetStore.PREFS_NAME, Context.MODE_PRIVATE)

    override fun readString(key: String): String? = prefs.getString(key, null)

    override fun writeString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun readStringSet(key: String): Set<String> = prefs.getStringSet(key, null) ?: emptySet()

    override fun writeStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }

    override fun readBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    override fun writeBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}

/**
 * Process-wide configuration for the game picker, onboarding and the
 * notification-permission prompt — SharedPreferences-backed so the value
 * survives process death and is checked on first launch.
 */
object TargetStore {

    const val PREFS_NAME = "automation_config"
    const val KEY_PROTECTED_APPS = "protected_apps"
    const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    const val KEY_NOTIFICATIONS_PROMPTED = "notifications_prompted"
    const val KEY_SERVICE_EVER_ENABLED = "service_ever_enabled"
    const val KEY_STRICTNESS_LEVELS = "strictness_levels"
    const val KEY_PREVIEW_MODE = "preview_mode"

    private val lock = Any()
    private var storage: KeyValueStore = InMemoryKeyValueStore()

    private val _protectedApps = MutableStateFlow<Set<String>>(emptySet())
    val protectedApps: StateFlow<Set<String>> = _protectedApps.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    private val _notificationsPrompted = MutableStateFlow(false)
    val notificationsPrompted: StateFlow<Boolean> = _notificationsPrompted.asStateFlow()

    private val _serviceEverEnabled = MutableStateFlow(false)
    val serviceEverEnabled: StateFlow<Boolean> = _serviceEverEnabled.asStateFlow()

    private val _strictnessLevels = MutableStateFlow<Map<String, Strictness>>(emptyMap())
    val strictnessLevels: StateFlow<Map<String, Strictness>> = _strictnessLevels.asStateFlow()

    private val _previewMode = MutableStateFlow(false)
    val previewMode: StateFlow<Boolean> = _previewMode.asStateFlow()

    fun hydrate(context: Context) {
        hydrate(SharedPreferencesStore(context))
    }

    fun hydrate(store: KeyValueStore) {
        synchronized(lock) {
            storage = store
            _protectedApps.value = store.readStringSet(KEY_PROTECTED_APPS)
            _isFirstLaunch.value = store.readBoolean(KEY_IS_FIRST_LAUNCH, default = true)
            _notificationsPrompted.value = store.readBoolean(KEY_NOTIFICATIONS_PROMPTED, default = false)
            _serviceEverEnabled.value = store.readBoolean(KEY_SERVICE_EVER_ENABLED, default = false)
            _strictnessLevels.value = decodeStrictness(store.readStringSet(KEY_STRICTNESS_LEVELS))
            _previewMode.value = store.readBoolean(KEY_PREVIEW_MODE, default = false)
        }
    }

    /** Adds or removes one app from the protected set; persists immediately. */
    fun setAppProtected(packageName: String, protected: Boolean) {
        synchronized(lock) {
            val next = LinkedHashSet(_protectedApps.value)
            if (protected) next += packageName else next -= packageName
            val frozen: Set<String> = next.toSet()
            storage.writeStringSet(KEY_PROTECTED_APPS, frozen)
            _protectedApps.value = frozen
        }
    }

    /** Called when the user finishes the setup slideshow. */
    fun completeFirstLaunch() {
        synchronized(lock) {
            storage.writeBoolean(KEY_IS_FIRST_LAUNCH, false)
            _isFirstLaunch.value = false
        }
    }

    /** The notification dialog is shown at most once per install. */
    fun markNotificationsPrompted() {
        synchronized(lock) {
            storage.writeBoolean(KEY_NOTIFICATIONS_PROMPTED, true)
            _notificationsPrompted.value = true
        }
    }

    /** Remembered forever so the offline watchdog can tell "off" from "never on". */
    fun markServiceEverEnabled() {
        synchronized(lock) {
            storage.writeBoolean(KEY_SERVICE_EVER_ENABLED, true)
            _serviceEverEnabled.value = true
        }
    }

    /** Per-game reaction level; anything absent or unknown is [Strictness.NORMAL]. */
    fun strictnessFor(packageName: String?): Strictness =
        _strictnessLevels.value[packageName] ?: Strictness.NORMAL

    fun setStrictness(packageName: String, strictness: Strictness) {
        synchronized(lock) {
            val next = _strictnessLevels.value + (packageName to strictness)
            _strictnessLevels.value = next
            storage.writeStringSet(
                KEY_STRICTNESS_LEVELS,
                next.map { (pkg, level) -> "$pkg=${level.key}" }.toSet(),
            )
        }
    }

    /** Preview mode: log-only dismissals, no BACK press. */
    fun setPreviewMode(enabled: Boolean) {
        synchronized(lock) {
            storage.writeBoolean(KEY_PREVIEW_MODE, enabled)
            _previewMode.value = enabled
        }
    }

    /** `"pkg=level"` pairs → map; malformed entries are dropped, never thrown. */
    private fun decodeStrictness(raw: Set<String>): Map<String, Strictness> =
        raw.mapNotNull { entry ->
            val separator = entry.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val packageName = entry.substring(0, separator)
            val level = Strictness.fromKey(entry.substring(separator + 1))
            packageName to level
        }.toMap()
}
