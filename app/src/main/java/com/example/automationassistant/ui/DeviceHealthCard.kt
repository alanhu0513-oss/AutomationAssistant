package com.example.automationassistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.automationassistant.R
import com.example.automationassistant.data.OemBrand
import com.example.automationassistant.data.OemGuide

/**
 * Per-brand survival guide: maps `Build.MANUFACTURER` to the battery-menu
 * path that keeps the background service alive on that family of devices,
 * with a jump-off button into the generic battery settings.
 */
@Composable
fun DeviceHealthCard(
    onOpenBatterySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = remember { OemGuide.brandFor(android.os.Build.MANUFACTURER) }
    val brandTitle = stringResource(
        when (brand) {
            OemBrand.MIUI -> R.string.brand_miui
            OemBrand.COLOROS -> R.string.brand_coloros
            OemBrand.ONEUI -> R.string.brand_oneui
            OemBrand.FUNTOUCH -> R.string.brand_funtouch
            OemBrand.STOCK -> R.string.brand_stock
        },
    )
    val steps = when (brand) {
        OemBrand.MIUI -> listOf(R.string.health_miui_1, R.string.health_miui_2, R.string.health_miui_3)
        OemBrand.COLOROS -> listOf(R.string.health_coloros_1, R.string.health_coloros_2, R.string.health_coloros_3)
        OemBrand.ONEUI -> listOf(R.string.health_oneui_1, R.string.health_oneui_2, R.string.health_oneui_3)
        OemBrand.FUNTOUCH -> listOf(R.string.health_funtouch_1, R.string.health_funtouch_2, R.string.health_funtouch_3)
        OemBrand.STOCK -> listOf(R.string.health_stock_1, R.string.health_stock_2, R.string.health_stock_3)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.health_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        GlassCard(modifier = Modifier.fillMaxWidth(), borderAccent = Neon.Amber) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.health_detected, brandTitle),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Neon.Amber,
                )
                Text(
                    text = stringResource(R.string.health_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                steps.forEachIndexed { index, stepRes ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Neon.Amber.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Neon.Amber,
                            )
                        }
                        Text(
                            text = stringResource(stepRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                FilledTonalButton(
                    onClick = onOpenBatterySettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = Neon.Amber,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.health_open_battery),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}
