package com.example.automationassistant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.automationassistant.R

enum class PickerTarget { PROTECTED, INTERRUPTER }

@Composable
fun ConfigSection(
    targetProtectedApp: String?,
    protectedLabel: String?,
    interrupterPackage: String?,
    interrupterLabel: String?,
    activePicker: PickerTarget,
    onChooseProtected: () -> Unit,
    onChooseInterrupter: () -> Unit,
    onClearProtected: () -> Unit,
    onClearInterrupter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ConfigRow(
            title = stringResource(R.string.config_protected_app),
            hint = stringResource(R.string.config_protected_app_hint),
            value = targetProtectedApp,
            label = protectedLabel,
            isActive = activePicker == PickerTarget.PROTECTED,
            onChoose = onChooseProtected,
            onClear = onClearProtected,
        )
        ConfigRow(
            title = stringResource(R.string.config_interrupter),
            hint = stringResource(R.string.config_interrupter_hint),
            value = interrupterPackage,
            label = interrupterLabel,
            isActive = activePicker == PickerTarget.INTERRUPTER,
            onChoose = onChooseInterrupter,
            onClear = onClearInterrupter,
        )
    }
}

@Composable
private fun ConfigRow(
    title: String,
    hint: String,
    value: String?,
    label: String?,
    isActive: Boolean,
    onChoose: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = label ?: stringResource(R.string.config_not_set),
                style = MaterialTheme.typography.bodyLarge,
                color = if (value == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (value != null && label != value) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                if (value != null) {
                    TextButton(onClick = onClear) {
                        Text(stringResource(R.string.config_clear))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                TextButton(onClick = onChoose) {
                    Text(stringResource(R.string.config_choose))
                }
            }
        }
    }
}
