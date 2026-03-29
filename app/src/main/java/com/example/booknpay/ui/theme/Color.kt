package com.example.booknpay.ui.theme

import androidx.compose.ui.graphics.Color

// ─── TibhukeBus brand palette ─────────────────────────────────────────────────
// Derived directly from the logo:
//   • Background:   #F0F0F2  (logo's cool off-white)
//   • "Tibhuke":   #1A1A1A  (near-black wordmark)
//   • "Bus" / arc / wheels: gradient #3A9BD5 → #0073B7 → #0A4FA0

// Primary — the exact blue from "Bus", the arc, and the wheel dots

 val BrandIndigo = Color(0xFF2B3896)
 val BrandDeep = Color(0xFF1A1D6E)
 val RouteOrange = Color(0xFFE65100) // For the destination dot
val Brand50   = Color(0xFFE6F3FA)
val Brand100  = Color(0xFFBBDEF0)
val Brand200  = Color(0xFF8DC8E6)
val Brand300  = Color(0xFF5EB2DB)
val Brand400  = Color(0xFF3A9BD5)   // arc highlight / lighter wheel
val Brand500  = Color(0xFF0073B7)   // primary brand blue — "Bus" text
val Brand600  = Color(0xFF005FA0)
val Brand700  = Color(0xFF0A4FA0)   // arc lower / darker wheel
val Brand800  = Color(0xFF063A78)
val Brand900  = Color(0xFF022450)

// Near-black — matches the "Tibhuke" portion of the wordmark
val Ink900  = Color(0xFF1A1A1A)
val Ink800  = Color(0xFF2C2C2C)
val Ink700  = Color(0xFF3F3F3F)
val Ink600  = Color(0xFF5A5A5A)
val Ink400  = Color(0xFF8A8A8A)
val Ink200  = Color(0xFFCCCCCC)
val Ink100  = Color(0xFFE5E5E5)
val Ink50   = Color(0xFFF0F0F2)   // logo background — splash screen bg

// Semantic — kept minimal to stay on-brand
val SemanticSuccess  = Color(0xFF2E7D32)
val SemanticSuccess10= Color(0xFFE8F5E9)
val SemanticError    = Color(0xFFC62828)
val SemanticError10  = Color(0xFFFFEBEE)
val SemanticWarning  = Color(0xFFE65100)
val SemanticWarning10= Color(0xFFFFF3E0)
val SemanticInfo     = Brand500

// ─── Legacy aliases (keep so existing files compile without changes) ───────────
val Blue900  = Brand900
val Blue800  = Brand800
val Blue700  = Brand700
val Blue600  = Brand600
val Blue500  = Brand500
val Blue400  = Brand400
val Blue200  = Brand200
val Blue100  = Brand100
val Blue50   = Brand50

// Amber accent — CTA buttons, price tags
val Amber700 = Color(0xFFF57F17)
val Amber600 = Color(0xFFFB8C00)
val Amber500 = Color(0xFFFFA000)
val Amber400 = Color(0xFFFFCA28)
val Amber100 = Color(0xFFFFF8E1)

// Teal
val Teal700  = Color(0xFF00695C)
val Teal600  = Color(0xFF00796B)
val Teal400  = Color(0xFF26A69A)
val Teal100  = Color(0xFFE0F2F1)

// Semantic shortcuts used across the UI
val Success  = SemanticSuccess
val Error    = SemanticError
val Warning  = SemanticWarning
val Info     = SemanticInfo

// Component tokens
val CardBackground      = Color(0xFFFFFFFF)
val CardBackgroundDark  = Color(0xFF1E1E2E)
val SurfaceVariantLight = Color(0xFFECF4FB)   // brand-tinted surface
val SurfaceVariantDark  = Color(0xFF1C2233)

// Status badge colors
val StatusConfirmed     = Brand500
val StatusConfirmed10   = Brand50
val StatusPending       = SemanticWarning
val StatusPending10     = SemanticWarning10
val StatusCancelled     = Ink600
val StatusCancelled10   = Ink100
val StatusPaid          = SemanticSuccess
val StatusPaid10        = SemanticSuccess10

// ─── Convenience raw colors ────────────────────────────────────────────────────
val White     = Color(0xFFFFFFFF)
val Gray950   = Ink900
val Gray900   = Ink800
val Gray800   = Ink700
val Gray700   = Ink600
val Gray600   = Ink600
val Gray400   = Ink400
val Gray200   = Ink200
val Gray100   = Ink100
val Gray50    = Ink50

// Splash-specific
val SplashBackground = Ink50      // #F0F0F2 — exact logo background colour
val SplashBlue       = Brand500   // #0073B7 — "Bus" text / loading dots