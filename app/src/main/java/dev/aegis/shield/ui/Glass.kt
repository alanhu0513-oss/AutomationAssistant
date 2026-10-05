package dev.aegis.shield.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Shared neon accents used by the glass edges and auras. */
object Neon {
    val Green = Color(0xFF3DFFC4)
    val Teal = Color(0xFF2DD4BF)
    val Red = Color(0xFFFF5C5C)
    val Amber = Color(0xFFFBBF24)
}

/**
 * Glassmorphic card: translucent frosted surface with a gradient hairline
 * border that fades around the corners — the signature container of the app.
 *
 * The frost is faked with a translucent white gradient over the midnight
 * background (Compose cannot sample the real backdrop); on Android 12+ the
 * optional [contentBlurred] layer blurs content drawn behind it via
 * [Modifier.blur].
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    borderAccent: Color = Neon.Green,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.07f),
                        Color.White.copy(alpha = 0.02f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        borderAccent.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.06f),
                    ),
                ),
                shape = shape,
            ),
        content = content,
    )
}

/**
 * Soft radial glow that breathes behind the status card — a glowing red aura
 * for standby, a pulsing neon-green one when the shield is operational.
 *
 * [Modifier.blur] degrades to a no-op below Android 12; the radial gradient
 * still reads as a halo there, so older devices keep the premium look.
 */
@Composable
fun PulsingAura(
    color: Color,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "aura")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (active) 0.75f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "auraAlpha",
    )

    Box(
        modifier = modifier
            .graphicsLayer { alpha = pulse }
            .blur(48.dp)
            .background(
                Brush.radialGradient(
                    listOf(color.copy(alpha = 0.55f), Color.Transparent),
                ),
            ),
    )
}
