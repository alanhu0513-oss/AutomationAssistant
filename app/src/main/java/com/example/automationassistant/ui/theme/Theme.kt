package com.example.automationassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0B2A4A),
    primaryContainer = Color(0xFF1B3A5C),
    onPrimaryContainer = Color(0xFFD6E7FF),
    secondary = Color(0xFF9AA0A6),
    onSecondary = Color(0xFF1C1B1F),
    background = Color(0xFF0E0F12),
    onBackground = Color(0xFFE6E6E9),
    surface = Color(0xFF16181C),
    onSurface = Color(0xFFE6E6E9),
    surfaceVariant = Color(0xFF1F2227),
    onSurfaceVariant = Color(0xFFA8ADB4),
    surfaceContainerLowest = Color(0xFF111317),
    surfaceContainerLow = Color(0xFF181A1F),
    surfaceContainer = Color(0xFF1B1E23),
    surfaceContainerHigh = Color(0xFF1F2228),
    surfaceContainerHighest = Color(0xFF25282F),
    outline = Color(0xFF2C3037),
    outlineVariant = Color(0xFF23262C),
    error = Color(0xFFF2B8B5)
)

@Composable
fun AutomationAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}
