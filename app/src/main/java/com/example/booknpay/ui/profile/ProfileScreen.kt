package com.example.booknpay.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.data.model.User
import com.example.booknpay.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Color tokens ──────────────────────────────────────────────────────────────
private val NavyBlue   = Color(0xFF2D3AC1)
private val DeepNavy   = Color(0xFF1A2070)
private val SoftBg     = Color(0xFFF0F1FB)
private val GoldAccent = Color(0xFFE6A817)
private val TealAccent = Color(0xFF009688)

// ── ViewModel ─────────────────────────────────────────────────────────────────

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    onNavigateToBack: ()->Unit,
    onNavigateToAuth:()->Unit,
    onLogout:  () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val travelPrefs = listOf(
        "Window Seat"      to Icons.Default.EventSeat,
        "Quiet Zone"       to Icons.Default.VolumeOff,
        "Vegan Meals"      to Icons.Default.Restaurant,
        "Real-time Alerts" to Icons.Default.Notifications
    )

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NavyBlue)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHero(
            displayName = state.user?.fullName ?: "Guest",
            email       = state.user?.email    ?: "",
            tier        = if (state.totalTrips >= 10) "PREMIUM MEMBER" else "STANDARD MEMBER"
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Stats
        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("TOTAL TRIPS",   "${state.totalTrips}", "+3 this month", Modifier.weight(1f))
            LoyaltyCard(state.loyaltyPts, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        LoyaltyProgress(state.loyaltyPts, 10000, Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(20.dp))

        // Personal details
        ProfileSection("Personal Details", "Update") {
            DetailRow(Icons.Default.Person, "FULL NAME",     state.user?.fullName ?: "—")
            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
            DetailRow(Icons.Default.Email,  "EMAIL ADDRESS", state.user?.email    ?: "—")
            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
            DetailRow(Icons.Default.Phone,  "MOBILE NUMBER", state.user?.phone    ?: "—")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Payments
        ProfileSection("Payments", "Add New") {
            PaymentRow(Icons.Default.PhoneAndroid, "Airtel Money", "Default method",  true)
            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
            PaymentRow(Icons.Default.CreditCard,   "TNM Mpamba",   "Mobile money",    false)
            Spacer(modifier = Modifier.height(10.dp))
            CashbackBanner()
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Travel prefs
        ProfileSection("Travel Preferences", null) {
            Spacer(modifier = Modifier.height(4.dp))
            PrefsGrid(travelPrefs)
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Logout
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clickable { viewModel.logout(onLogout) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Logout, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout from account", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHero(displayName: String, email: String, tier: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(DeepNavy, NavyBlue)))
            .padding(horizontal = 24.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(88.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(50.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = GoldAccent) {
                Text(tier, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.8.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(email, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = {},
                shape   = RoundedCornerShape(24.dp),
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border  = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Profile", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, delta: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(delta, style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
private fun LoyaltyCard(points: Int, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("LOYALTY POINTS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("%,d".format(points), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = GoldAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Text("XP", style = MaterialTheme.typography.labelSmall, color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
private fun LoyaltyProgress(current: Int, target: Int, modifier: Modifier) {
    val animated by animateFloatAsState((current.toFloat() / target).coerceIn(0f, 1f), tween(1200, easing = FastOutSlowInEasing), label = "pts")
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(NavyBlue), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("NEXT REWARD", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(progress = { animated }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = GoldAccent, trackColor = Color.White.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("%,d pts until Free Upgrade".format((target - current).coerceAtLeast(0)), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun ProfileSection(title: String, actionLabel: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (actionLabel != null) Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = NavyBlue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable {})
        }
        Spacer(modifier = Modifier.height(10.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(SoftBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = NavyBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 0.5.sp)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PaymentRow(icon: ImageVector, label: String, subtitle: String, isDefault: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(NavyBlue), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (isDefault) Icon(Icons.Default.CheckCircle, null, tint = NavyBlue, modifier = Modifier.size(20.dp))
        else Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CashbackBanner() {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TealAccent.copy(alpha = 0.1f)).clickable {}.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(TealAccent.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Savings, null, tint = TealAccent, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Save 5% on every booking", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TealAccent)
            Text("Activate automatic Tibhuke Credits", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TealAccent, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PrefsGrid(prefs: List<Pair<String, ImageVector>>) {
    prefs.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { (label, icon) ->
                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(SoftBg).clickable {}.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, null, tint = NavyBlue, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                }
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}