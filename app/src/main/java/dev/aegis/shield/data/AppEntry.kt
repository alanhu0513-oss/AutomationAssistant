package dev.aegis.shield.data

import androidx.compose.ui.graphics.ImageBitmap

/**
 * One launchable app in the visual selector: official icon + friendly name.
 * Package name stays as the stable key — it is never rendered to the user.
 */
data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap? = null,
)
