package com.example.automationassistant.automation

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Every PackageManager lookup the shield needs, isolated from the event
 * loop: launcher packages (never dismiss home), the default dialer (never
 * dismiss a call), a cached runtime `FLAG_SYSTEM` resolver, and label
 * resolution for the activity log.
 *
 * All lookups are dynamic and brand-agnostic — no vendor strings live here.
 */
class WindowResolvers(private val packageManager: PackageManager) {

    private val systemnessCache = ConcurrentHashMap<String, Boolean>()

    /** The launcher package(s); guards against dismissing the home screen. */
    fun homePackages(): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return runCatching {
            packageManager.queryIntentActivities(homeIntent, 0)
                .mapNotNull { it.activityInfo?.packageName }
                .toSet()
        }.getOrElse { emptySet() }
    }

    /** The default dialer; an incoming call must never be dismissed. */
    fun exemptPackages(): Set<String> = runCatching {
        val resolved = packageManager.resolveActivity(Intent(Intent.ACTION_DIAL), 0)
        setOfNotNull(resolved?.activityInfo?.packageName)
    }.getOrElse { emptySet() }

    /** Runtime `FLAG_SYSTEM` check with a per-package cache — no brand lists. */
    fun isSystemPackage(packageName: String): Boolean {
        systemnessCache[packageName]?.let { return it }
        val result = try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (t: Throwable) {
            Log.e(TAG, "Systemness check failed for $packageName", t)
            false
        }
        systemnessCache[packageName] = result
        return result
    }

    /** Display name for a package, falling back to the package itself. */
    fun labelOf(packageName: String): String = runCatching {
        packageManager
            .getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
            .toString()
    }.getOrDefault(packageName)

    private companion object {
        private const val TAG = "GamingShield"
    }
}
