package dev.aegis.shield.automation

import android.content.Context
import android.content.SharedPreferences

class TargetStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aegis_shield_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PROTECTED_APPS = "protected_apps"
        private const val KEY_PREVIEW_MODE = "preview_mode"
        private const val KEY_DISMISS_COUNT = "dismiss_count"
    }

    fun getProtectedApps(): Set<String> {
        return prefs.getStringSet(KEY_PROTECTED_APPS, emptySet()) ?: emptySet()
    }

    fun setProtectedApps(apps: Set<String>) {
        prefs.edit().putStringSet(KEY_PROTECTED_APPS, apps).apply()
    }

    fun isPreviewMode(): Boolean {
        return prefs.getBoolean(KEY_PREVIEW_MODE, false)
    }

    fun setPreviewMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PREVIEW_MODE, enabled).apply()
    }

    fun getDebounceMs(packageName: String): Long {
        val level = prefs.getString("strictness_$packageName", "normal")
        return when (level) {
            "strict" -> 100L
            "gentle" -> 1000L
            else -> 400L
        }
    }

    fun incrementDismissalCount() {
        val current = prefs.getInt(KEY_DISMISS_COUNT, 0)
        prefs.edit().putInt(KEY_DISMISS_COUNT, current + 1).apply()
    }
}
