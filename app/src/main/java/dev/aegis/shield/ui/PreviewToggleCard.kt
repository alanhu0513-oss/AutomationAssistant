package dev.aegis.shield.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.aegis.shield.R

/**
 * Preview mode: when on, the engine still watches, decides and logs every
 * would-be dismissal — but never presses BACK. The honest way for a curious
 * (or skeptical) player to see exactly what the shield would block.
 */
@Composable
fun PreviewToggleCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderAccent = if (enabled) Neon.Amber else Neon.Teal,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.preview_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) Neon.Amber else MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.preview_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Neon.Amber,
                    checkedBorderColor = Neon.Amber,
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                ),
            )
        }
    }
}
