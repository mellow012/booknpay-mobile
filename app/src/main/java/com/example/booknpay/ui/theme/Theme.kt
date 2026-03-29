package com.example.booknpay.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Light scheme ─────────────────────────────────────────────────────────────
// Primary family: Brand500 (#0073B7) — the logo blue
private val LightColorScheme = lightColorScheme(
    // Primary
    primary              = Brand500,        // #0073B7
    onPrimary            = White,
    primaryContainer     = Brand50,         // #E6F3FA  — light tinted surfaces
    onPrimaryContainer   = Brand800,        // #063A78

    // Secondary — darker brand blue for secondary actions
    secondary            = Brand700,        // #0A4FA0
    onSecondary          = White,
    secondaryContainer   = Brand100,        // #BBDEf0
    onSecondaryContainer = Brand800,

    // Tertiary — amber for price tags, CTAs that need warmth
    tertiary             = Amber600,
    onTertiary           = White,
    tertiaryContainer    = Amber100,
    onTertiaryContainer  = Amber700,

    // Backgrounds
    background           = Ink50,           // #F0F0F2 — matches logo background exactly
    onBackground         = Ink900,          // #1A1A1A — matches "Tibhuke" wordmark

    // Surfaces
    surface              = White,
    onSurface            = Ink800,
    surfaceVariant       = SurfaceVariantLight, // #ECF4FB — brand-tinted
    onSurfaceVariant     = Ink600,

    // Error
    error                = SemanticError,
    onError              = White,
    errorContainer       = SemanticError10,
    onErrorContainer     = SemanticError,

    // Outline
    outline              = Ink200,          // subtle borders
    outlineVariant       = Ink100,
)

// ─── Dark scheme ──────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Brand400,        // #3A9BD5 — lighter for dark bg contrast
    onPrimary            = Brand900,
    primaryContainer     = Brand800,
    onPrimaryContainer   = Brand100,

    secondary            = Brand300,
    onSecondary          = Brand900,
    secondaryContainer   = Brand700,
    onSecondaryContainer = Brand100,

    tertiary             = Amber400,
    onTertiary           = Ink900,
    tertiaryContainer    = Amber700,
    onTertiaryContainer  = Amber100,

    background           = Color(0xFF111418), // near-black with very slight blue cast
    onBackground         = Ink50,

    surface              = CardBackgroundDark,
    onSurface            = Ink100,
    surfaceVariant       = SurfaceVariantDark,
    onSurfaceVariant     = Ink200,

    error                = SemanticError10,
    onError              = SemanticError,

    outline              = Ink700,
    outlineVariant       = Ink800,
)

// ─── Theme entry point ────────────────────────────────────────────────────────
@Composable
fun BooknpayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar matches the app background for a seamless edge-to-edge look
            window.statusBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Light status bar icons on light theme, dark icons on dark theme
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}