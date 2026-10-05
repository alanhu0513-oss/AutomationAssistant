package dev.aegis.shield.ui

import android.content.Context
import android.os.PowerManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.aegis.shield.R

/** The three honest states of the shield, rendered as a glass status card. */
enum class StatusState { STANDBY, WAITING, OPERATIONAL }

/**
 * The centerpiece glassmorphic status card.
 *
 * Inactive → glowing red aura + "Engine Standby".
 * Active   → pulsing neon-green aura + "Shield Operational".
 * (Plus an amber middle state when the engine runs but no game is selected,
 * so the card never lies to the user.)
 */
@Composable
fun StatusCard(
    isRunning: Boolean,
    selectedGameCount: Int,
    blockedCount: Int,
    onKeepRunning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = when {
        !isRunning -> StatusState.STANDBY
        selectedGameCount == 0 -> StatusState.WAITING
        else -> StatusState.OPERATIONAL
    }

    val accent by animateColorAsState(
        targetValue = when (state) {
            StatusState.STANDBY -> Neon.Red
            StatusState.WAITING -> Neon.Amber
            StatusState.OPERATIONAL -> Neon.Green
        },
        animationSpec = tween(durationMillis = 400),
        label = "statusAccent",
    )

    val title = when (state) {
        StatusState.STANDBY -> stringResource(R.string.status_standby_title)
        StatusState.WAITING -> stringResource(R.string.status_waiting_title)
        StatusState.OPERATIONAL -> stringResource(R.string.status_operational_title)
    }

    val hint = when (state) {
        StatusState.STANDBY -> stringResource(R.string.status_standby_hint)
        StatusState.WAITING -> stringResource(R.string.status_waiting_hint)
        StatusState.OPERATIONAL -> stringResource(R.string.status_operational_hint)
    }

    val context = LocalContext.current
    val ignoringBattery = remember(context) {
        runCatching {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }.getOrNull()
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Breathing radial glow behind the glass — red for standby, neon green
        // when operational. Blur degrades gracefully below Android 12.
        PulsingAura(
            color = accent,
            active = state == StatusState.OPERATIONAL,
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderAccent = accent,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusOrb(accent = accent, icon = state.icon)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (state == StatusState.OPERATIONAL && blockedCount > 0) {
                    Text(
                        text = stringResource(R.string.status_blocked_count, blockedCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    )
                }

                if (state == StatusState.OPERATIONAL && ignoringBattery == false) {
                    TextButton(
                        onClick = onKeepRunning,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.keep_running),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

private val StatusState.icon: ImageVector
    get() = when (this) {
        StatusState.STANDBY -> Icons.Outlined.Lock
        StatusState.WAITING -> Icons.Outlined.PlayArrow
        StatusState.OPERATIONAL -> Icons.Outlined.CheckCircle
    }

/** Icon orb with a blurred halo in the status accent color. */
@Composable
private fun StatusOrb(accent: Color, icon: ImageVector) {
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .blur(14.dp)
                .background(
                    Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.7f), Color.Transparent),
                    ),
                ),
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = accent,
        )
    }
}
