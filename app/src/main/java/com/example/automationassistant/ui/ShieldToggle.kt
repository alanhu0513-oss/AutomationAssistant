package com.example.automationassistant.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.example.automationassistant.R

/**
 * Premium smooth-sliding toggle: a tactile glass capsule whose thumb springs
 * into a glowing neon position when the engine runs. Tapping anywhere on it
 * opens the phone's Accessibility settings — it reflects state, it doesn't
 * silently flip it (Android only enables the engine there).
 */
@Composable
fun ShieldToggle(
    running: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackWidth = 62.dp
    val trackHeight = 36.dp
    val thumbInset = 4.dp
    val thumbSize = 28.dp

    val fraction by animateFloatAsState(
        targetValue = if (running) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "toggleFraction",
    )
    val trackColor by animateColorAsState(
        targetValue = if (running) Neon.Green else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(durationMillis = 300),
        label = "toggleTrack",
    )
    val thumbColor by animateColorAsState(
        targetValue = if (running) Color(0xFF03261C) else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "toggleThumb",
    )
    val glowElevation by animateDpAsState(
        targetValue = if (running) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "toggleGlow",
    )
    val edgeAlpha by animateFloatAsState(
        targetValue = if (running) 0.7f else 0.12f,
        animationSpec = tween(durationMillis = 300),
        label = "toggleEdge",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
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
                        Neon.Green.copy(alpha = edgeAlpha),
                        Color.White.copy(alpha = 0.08f),
                        Neon.Teal.copy(alpha = edgeAlpha * 0.6f),
                    ),
                ),
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(
                role = Role.Switch,
                onClick = onOpenSettings,
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(if (running) R.string.toggle_shield_on else R.string.toggle_shield_off),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (running) Neon.Green else MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.toggle_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(
            modifier = Modifier
                .width(trackWidth)
                .size(width = trackWidth, height = trackHeight)
                .shadow(glowElevation, CircleShape, ambientColor = Neon.Green, spotColor = Neon.Green)
                .clip(CircleShape)
                .background(trackColor),
            contentAlignment = Alignment.CenterStart,
        ) {
            val thumbX by animateDpAsState(
                targetValue = lerp(thumbInset, trackWidth - thumbSize - thumbInset, fraction),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "toggleThumbX",
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbX.roundToPx(), 0) }
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(thumbColor),
            )
        }
    }
}
