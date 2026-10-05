package dev.aegis.shield.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.aegis.shield.R
import dev.aegis.shield.automation.Strictness
import dev.aegis.shield.data.AppEntry

/** Friendly "Find an app" filter — label-only, never package names. */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.games_search_hint)) },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedBorderColor = Neon.Green.copy(alpha = 0.6f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

/**
 * Sleek rounded list item: high-resolution icon + app name. A smooth
 * elevation lift and a neon gradient edge mark it as protected; protected
 * rows also carry a tappable reaction-level chip (Normal / Gentle / Strict).
 */
@Composable
fun GameRow(
    entry: AppEntry,
    isProtected: Boolean,
    strictness: Strictness,
    onClick: () -> Unit,
    onCycleStrictness: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val elevation by animateDpAsState(
        targetValue = if (isProtected) 14.dp else 4.dp,
        animationSpec = tween(durationMillis = 300),
        label = "rowElevation",
    )
    val labelColor by animateColorAsState(
        targetValue = if (isProtected) Neon.Green else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(durationMillis = 300),
        label = "rowLabel",
    )
    val borderColor by animateFloatAsState(
        targetValue = if (isProtected) 1f else 0.12f,
        animationSpec = tween(durationMillis = 300),
        label = "rowBorder",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Neon.Green,
                spotColor = Neon.Green,
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.White.copy(alpha = 0.015f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Neon.Green.copy(alpha = borderColor),
                        Neon.Teal.copy(alpha = borderColor * 0.5f),
                        Color.White.copy(alpha = 0.04f),
                    ),
                ),
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(entry = entry, modifier = Modifier.size(46.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = entry.label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isProtected) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (isProtected) {
            StrictnessChip(strictness = strictness, onCycle = onCycleStrictness)
        }
    }
}

/** Small tappable pill showing (and cycling) the reaction level of a protected game. */
@Composable
private fun StrictnessChip(strictness: Strictness, onCycle: () -> Unit) {
    val color = when (strictness) {
        Strictness.NORMAL -> Neon.Teal
        Strictness.GENTLE -> Neon.Green
        Strictness.STRICT -> Neon.Amber
    }
    val label = stringResource(
        when (strictness) {
            Strictness.NORMAL -> R.string.strictness_normal
            Strictness.GENTLE -> R.string.strictness_gentle
            Strictness.STRICT -> R.string.strictness_strict
        },
    )
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(onClick = onCycle)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun AppIcon(entry: AppEntry, modifier: Modifier = Modifier) {
    val icon = entry.icon
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = stringResource(R.string.cd_app_icon),
            modifier = modifier.clip(RoundedCornerShape(13.dp)),
        )
    } else {
        // Fallback tile: first letter of the app name (still no jargon).
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = entry.label.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
