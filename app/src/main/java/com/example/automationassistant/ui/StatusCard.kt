package com.example.automationassistant.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.automationassistant.R
import com.example.automationassistant.automation.WindowSnapshot

@Composable
fun StatusCard(
    isRunning: Boolean,
    enabled: Boolean,
    watchingLabel: String?,
    foregroundPackage: String?,
    lastWindow: WindowSnapshot?,
    lastDismissed: String?,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dotColor by animateColorAsState(
        targetValue = when {
            !isRunning -> Color(0xFF6B7280)
            !enabled -> Color(0xFFF59E0B)
            else -> Color(0xFF34D399)
        },
        animationSpec = tween(durationMillis = 400),
        label = "statusDot",
    )

    val detailText = when {
        !isRunning -> stringResource(R.string.status_service_off)
        !enabled -> stringResource(R.string.status_paused)
        watchingLabel == null -> stringResource(R.string.status_missing_interrupter)
        else -> stringResource(R.string.status_watching, watchingLabel)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isRunning) {
                            stringResource(R.string.status_active)
                        } else {
                            stringResource(R.string.status_inactive)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = isRunning,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = foregroundPackage?.let {
                        stringResource(R.string.status_foreground, it)
                    } ?: stringResource(R.string.status_foreground_unknown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = lastWindow?.let {
                        stringResource(R.string.status_last_window, it.describe())
                    } ?: stringResource(R.string.status_no_window),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = lastDismissed?.let {
                        stringResource(R.string.status_last_dismissed, it)
                    } ?: stringResource(R.string.status_no_dismissals),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun WindowSnapshot.describe(): String =
    className?.let { "$packageName/$it" } ?: packageName
