package dev.aegis.shield.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aegis.shield.R
import kotlinx.coroutines.launch

private data class Slide(
    val icon: ImageVector,
    val titleRes: Int,
    val bodyRes: Int,
)

private val OnboardingSlides = listOf(
    Slide(Icons.Outlined.Lock, R.string.slide1_title, R.string.slide1_body),
    Slide(Icons.Outlined.Settings, R.string.slide2_title, R.string.slide2_body),
    Slide(Icons.Filled.Lock, R.string.slide3_title, R.string.slide3_body),
)

/**
 * First-launch walkthrough: three swipeable glass slides, neon dot indicators,
 * one plain-English action per step. Shown exactly once — completing it
 * persists `is_first_launch = false` via [TargetStore][dev.aegis.shield.automation.TargetStore].
 */
@Composable
fun OnboardingScreen(
    onOpenAccessibilitySettings: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingSlides.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            SlideContent(slide = OnboardingSlides[page])
        }

        PagerDots(
            pageCount = OnboardingSlides.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(28.dp))

        when (pagerState.currentPage) {
            0 -> PrimaryButton(
                text = stringResource(R.string.slide1_next),
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(1) }
                },
            )

            1 -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.slide2_grant),
                    onClick = onOpenAccessibilitySettings,
                )
                TextButton(onClick = {
                    scope.launch { pagerState.animateScrollToPage(2) }
                }) {
                    Text(
                        text = stringResource(R.string.slide2_next),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> PrimaryButton(
                text = stringResource(R.string.slide3_finish),
                onClick = onFinished,
            )
        }
    }
}

@Composable
private fun SlideContent(slide: Slide) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GlassCard(
            shape = RoundedCornerShape(36.dp),
            borderAccent = Neon.Green,
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Neon.Green.copy(alpha = 0.28f), Color.Transparent),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Neon.Green,
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = stringResource(slide.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(slide.bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Page indicator: the active page grows into a neon pill. */
@Composable
private fun PagerDots(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            val width by animateDpAsState(
                targetValue = if (active) 26.dp else 8.dp,
                animationSpec = tween(durationMillis = 250),
                label = "dotWidth",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (active) Neon.Green
                        else MaterialTheme.colorScheme.outline,
                    ),
            )
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Neon.Green,
            contentColor = Color(0xFF03261C),
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
