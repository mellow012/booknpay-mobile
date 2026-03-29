package com.example.booknpay.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.booknpay.R
import com.example.booknpay.ui.navigation.Screen
import com.example.booknpay.ui.theme.SplashBackground
import com.example.booknpay.ui.theme.SplashBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAuth: () -> Unit,
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // ── Logo pop-in ───────────────────────────────────────────────────────────
    val scale by animateFloatAsState(
        targetValue   = if (state.isAnimating) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    // ── Fade in ───────────────────────────────────────────────────────────────
    val alpha by animateFloatAsState(
        targetValue   = if (state.isAnimating) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label         = "logoAlpha"
    )

    // ── Navigate when destination is set ─────────────────────────────────────
    LaunchedEffect(state.navigationDestination) {
        state.navigationDestination?.let { destination ->
            when (destination) {
                NavigationDestination.HOME -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                NavigationDestination.AUTH -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                NavigationDestination.ONBOARDING -> {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
        }
    }

    // ── Root: same colour as the logo's own background ────────────────────────
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(SplashBackground),   // #F0F0F2 — exact logo bg
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier             = Modifier
                .padding(32.dp)
                .scale(scale)
                .alpha(alpha),
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.Center
        ) {
            // ── Logo: no container shape, no border, no shadow ────────────────
            // The image already has the correct background baked in, so we simply
            // render it at natural size — the screen background matches perfectly.
            Image(
                painter            = painterResource(id = R.drawable.tibhukebus_logo),
                contentDescription = "TibhukeBus",
                modifier           = Modifier
                    .width(280.dp)       // fill most of the screen width
                    .aspectRatio(1.6f)   // preserve the logo's natural w:h ratio
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Tagline ───────────────────────────────────────────────────────
            Text(
                text       = "Your Journey, Our Priority",
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color      = SplashBlue      // #0073B7 — matches "Bus" text in logo
            )

            Spacer(modifier = Modifier.height(52.dp))

            // ── Loading dots ──────────────────────────────────────────────────
            if (state.isLoading) {
                LoadingDots(dotColor = SplashBlue)
            }

            state.error?.let { err ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text  = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = SplashBlue.copy(alpha = 0.6f)
                )
            }
        }

        // ── Version number ────────────────────────────────────────────────────
        Text(
            text     = "v${state.appVersion}",
            style    = MaterialTheme.typography.labelSmall,
            color    = Color(0xFF1A1A1A).copy(alpha = 0.25f),  // matches "Tibhuke" ink, faded
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(alpha)
        )
    }
}

// ── Animated loading dots ─────────────────────────────────────────────────────
@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    dotColor: Color    = SplashBlue
) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val dotAlpha by transition.animateFloat(
                initialValue  = 0.2f,
                targetValue   = 1f,
                animationSpec = infiniteRepeatable(
                    animation          = tween(600),
                    repeatMode         = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = dotAlpha))
            )
            if (index < 2) Spacer(modifier = Modifier.width(9.dp))
        }
    }
}