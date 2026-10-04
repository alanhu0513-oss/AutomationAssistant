package com.example.automationassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Luxury dark aesthetic: a deep midnight-slate canvas with neon green/teal
 * accents. Pure dark scheme — the product only ever ships in dark mode.
 */
private val MidnightScheme = darkColorScheme(
    primary = Color(0xFF3DFFC4),
    onPrimary = Color(0xFF03261C),
    primaryContainer = Color(0xFF0B3D2E),
    onPrimaryContainer = Color(0xFF9DF7E1),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF03261C),
    secondaryContainer = Color(0xFF113F37),
    onSecondaryContainer = Color(0xFFA7F3E4),
    tertiary = Color(0xFF7DD3FC),
    onTertiary = Color(0xFF032033),
    background = Color(0xFF080B10),
    onBackground = Color(0xFFE7EDF3),
    surface = Color(0xFF0E141B),
    onSurface = Color(0xFFE7EDF3),
    surfaceVariant = Color(0xFF151C25),
    onSurfaceVariant = Color(0xFF93A1AF),
    surfaceContainerLowest = Color(0xFF0A0F15),
    surfaceContainerLow = Color(0xFF11171F),
    surfaceContainer = Color(0xFF141B24),
    surfaceContainerHigh = Color(0xFF19212B),
    surfaceContainerHighest = Color(0xFF1F2833),
    outline = Color(0xFF2A3542),
    outlineVariant = Color(0xFF212B36),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF3A0A0A),
    errorContainer = Color(0xFF4A1518),
    onErrorContainer = Color(0xFFFFB4B4),
)

@Composable
fun AutomationAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MidnightScheme,
        content = content,
    )
}
