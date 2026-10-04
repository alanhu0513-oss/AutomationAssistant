package com.example.automationassistant.data

import android.content.Intent
import android.content.pm.PackageManager

class AppRepository(
    private val packageManager: PackageManager,
    private val selfPackage: String,
) {

    fun loadLauncherApps(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
                val label = resolveInfo.loadLabel(packageManager)?.toString()
                    ?.ifBlank { packageName }
                    ?: packageName
                AppEntry(packageName = packageName, label = label, isSelf = packageName == selfPackage)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
