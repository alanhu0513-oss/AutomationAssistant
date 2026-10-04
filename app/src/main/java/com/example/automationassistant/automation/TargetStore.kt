package com.example.automationassistant.automation

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface KeyValueStore {
    fun read(key: String): String?
    fun write(key: String, value: String?)
}

class InMemoryKeyValueStore(initial: Map<String, String> = emptyMap()) : KeyValueStore {
    private val values: MutableMap<String, String> = initial.toMutableMap()

    override fun read(key: String): String? = values[key]

    override fun write(key: String, value: String?) {
        if (value == null) {
            values.remove(key)
        } else {
            values[key] = value
        }
    }
}

class SharedPreferencesStore(context: Context) : KeyValueStore {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(TargetStore.PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(key: String): String? = prefs.getString(key, null)

    override fun write(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }
}

object TargetStore {

    const val PREFS_NAME = "automation_config"
    const val KEY_TARGET_PROTECTED_APP = "target_protected_app"
    const val KEY_INTERRUPTER_PACKAGE_NAME = "interrupter_package_name"

    private val lock = Any()
    private var storage: KeyValueStore = InMemoryKeyValueStore()

    private val _targetProtectedApp = MutableStateFlow<String?>(null)
    val targetProtectedApp: StateFlow<String?> = _targetProtectedApp.asStateFlow()

    private val _interrupterPackageName = MutableStateFlow<String?>(null)
    val interrupterPackageName: StateFlow<String?> = _interrupterPackageName.asStateFlow()

    fun hydrate(context: Context) {
        hydrate(SharedPreferencesStore(context))
    }

    fun hydrate(store: KeyValueStore) {
        synchronized(lock) {
            storage = store
            _targetProtectedApp.value = store.read(KEY_TARGET_PROTECTED_APP)
            _interrupterPackageName.value = store.read(KEY_INTERRUPTER_PACKAGE_NAME)
        }
    }

    fun setTargetProtectedApp(value: String?) {
        synchronized(lock) {
            storage.write(KEY_TARGET_PROTECTED_APP, value)
            _targetProtectedApp.value = value
        }
    }

    fun setInterrupterPackageName(value: String?) {
        synchronized(lock) {
            storage.write(KEY_INTERRUPTER_PACKAGE_NAME, value)
            _interrupterPackageName.value = value
        }
    }
}
