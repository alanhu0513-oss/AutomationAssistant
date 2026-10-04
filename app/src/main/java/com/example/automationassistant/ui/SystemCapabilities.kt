package com.example.automationassistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automationassistant.R

/**
 * "System Capabilities & Limits" — the transparency section of the dashboard.
 *
 * States, in plain language, exactly where the shield is bullet-proof, where
 * Android's own security model stops it, and the one-in-a-hundred reaction
 * caveat. Keeping this permanently on screen (not buried in a README) sets
 * honest expectations for every user, technical or not.
 */
@Composable
fun SystemCapabilitiesCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.capabilities_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CapabilityRow(
                    accent = Neon.Green,
                    icon = Icons.Filled.CheckCircle,
                    label = stringResource(R.string.capability_perfect_label),
                    body = stringResource(R.string.capability_perfect_body),
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                CapabilityRow(
                    accent = Neon.Red,
                    icon = Icons.Filled.Warning,
                    label = stringResource(R.string.capability_limit_label),
                    body = stringResource(R.string.capability_limit_body),
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                CapabilityRow(
                    accent = Neon.Amber,
                    icon = Icons.Filled.Info,
                    label = stringResource(R.string.capability_slow_label),
                    body = stringResource(R.string.capability_slow_body),
                )
            }
        }
    }
}

@Composable
private fun CapabilityRow(
    accent: Color,
    icon: ImageVector,
    label: String,
    body: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
        }
    }
}
