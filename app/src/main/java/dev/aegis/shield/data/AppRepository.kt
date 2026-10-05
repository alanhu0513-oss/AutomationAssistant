package dev.aegis.shield.data

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Loads the visual game picker: every launchable app the user installed
 * themselves (no system apps, no ourselves), with its official icon
 * pre-scaled to a small square. Failures are contained — the caller gets a
 * [Result] and shows a friendly snackbar instead of crashing.
 */
class AppRepository(
    private val packageManager: PackageManager,
    private val selfPackage: String,
) {

    fun loadUserApps(): Result<List<AppEntry>> {
        return try {
            Result.success(queryApps())
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load user apps", t)
            Result.failure(t)
        }
    }

    private fun queryApps(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val candidates = packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val appInfo = activityInfo.applicationInfo ?: return@mapNotNull null
                CandidateApp(
                    packageName = activityInfo.packageName,
                    label = resolveInfo.loadLabel(packageManager)?.toString().orEmpty(),
                    isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                )
            }
        return AppSelection.select(candidates, selfPackage).map { candidate ->
            AppEntry(
                packageName = candidate.packageName,
                label = candidate.label,
                icon = runCatching {
                    packageManager.getApplicationIcon(candidate.packageName)
                        .toIconBitmap(ICON_SIZE_PX)
                }.getOrNull(),
            )
        }
    }

    /** Draws any drawable into a fixed square, preserving its aspect ratio. */
    private fun Drawable.toIconBitmap(size: Int): ImageBitmap {
        val intrinsicW = intrinsicWidth.takeIf { it > 0 } ?: size
        val intrinsicH = intrinsicHeight.takeIf { it > 0 } ?: size
        val scale = minOf(size.toFloat() / intrinsicW, size.toFloat() / intrinsicH)
        val width = (intrinsicW * scale).toInt().coerceAtLeast(1)
        val height = (intrinsicH * scale).toInt().coerceAtLeast(1)
        val left = (size - width) / 2
        val top = (size - height) / 2
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(left, top, left + width, top + height)
        draw(canvas)
        return bitmap.asImageBitmap()
    }

    private companion object {
        private const val TAG = "GamingShield"

        /** 144 px covers a 44 dp icon on xxhdpi with headroom for xxxhdpi. */
        const val ICON_SIZE_PX = 144
    }
}
