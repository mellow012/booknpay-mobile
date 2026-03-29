package com.example.booknpay.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

// ─── Data model ───────────────────────────────────────────────────────────────
private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val highlight: String,
    val accentColor: Color,
    val backgroundTint: Color
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.DirectionsBus,
        title = "Travel with\nconfidence",
        subtitle = "Search hundreds of intercity bus routes across Malawi. Compare operators, prices, and departure times — all in one place.",
        highlight = "confidence",
        accentColor = Color(0xFF0073B7),
        backgroundTint = Color(0xFFE6F3FA)
    ),
    OnboardingPage(
        icon = Icons.Default.EventSeat,
        title = "Pick your\nperfect seat",
        subtitle = "Choose exactly where you sit with our interactive seat map. Window, aisle, upper or lower deck — your call.",
        highlight = "perfect seat",
        accentColor = Color(0xFF0A4FA0),
        backgroundTint = Color(0xFFDFE0FF)
    ),
    OnboardingPage(
        icon = Icons.Default.QrCode2,
        title = "Board with\nyour phone",
        subtitle = "Your digital ticket is always ready. Show your QR code to the conductor — no printing needed, no queues.",
        highlight = "your phone",
        accentColor = Color(0xFF2E7D32),
        backgroundTint = Color(0xFFEAF3DE)
    )
)

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    // Correct way to call setHasSeenOnboarding
    val onFinishWithFlag = {
        viewModel.setHasSeenOnboarding()
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated background
        AnimatedPageBackground(pagerState)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(visible = !isLastPage) {
                    TextButton(onClick = onFinishWithFlag) {
                        Text(
                            "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(page = pages[pageIndex])
            }

            // Bottom Controls
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot indicators
                PagerIndicators(pagerState = pagerState, count = pages.size)

                Spacer(modifier = Modifier.height(40.dp))

                // Main CTA Button
                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinishWithFlag()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].accentColor
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Get started" else "Next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isLastPage) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                }

                // "Already have an account" link (only on last page)
                if (isLastPage) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onFinishWithFlag) {
                        Text(
                            "Already have an account? Sign in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─── Per-page content ─────────────────────────────────────────────────────────
@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(page.backgroundTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = page.accentColor
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "0${pages.indexOf(page) + 1}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = page.accentColor,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = MaterialTheme.typography.headlineLarge.lineHeight
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

// ─── Animated background ─────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun AnimatedPageBackground(pagerState: PagerState) {
    val fraction = pagerState.currentPageOffsetFraction
    val current = pages[pagerState.currentPage]
    val next = pages.getOrNull(
        (pagerState.currentPage + if (fraction > 0) 1 else -1).coerceIn(0, pages.lastIndex)
    )

    val tint = if (next != null && fraction != 0f) {
        lerp(current.backgroundTint, next.backgroundTint, kotlin.math.abs(fraction))
    } else {
        current.backgroundTint
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.55f)
            .alpha(0.35f)
            .background(
                Brush.radialGradient(
                    colors = listOf(tint, Color.Transparent),
                    radius = 600f
                )
            )
    )
}

// ─── Pager Indicators ────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun PagerIndicators(pagerState: PagerState, count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { index ->
            val isSelected = pagerState.currentPage == index
            val width by animateDpAsState(
                targetValue = if (isSelected) 28.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dot_width"
            )

            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) pages[pagerState.currentPage].accentColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
            )
        }
    }
}

// ─── Color lerp helper ───────────────────────────────────────────────────────
private fun lerp(a: Color, b: Color, t: Float): Color {
    val tc = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * tc,
        green = a.green + (b.green - a.green) * tc,
        blue = a.blue + (b.blue - a.blue) * tc,
        alpha = 1f
    )
}