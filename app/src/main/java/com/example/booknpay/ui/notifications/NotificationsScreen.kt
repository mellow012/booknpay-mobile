package com.example.booknpay.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booknpay.data.model.Notification
import java.text.SimpleDateFormat
import java.util.*

// ── Color tokens ──────────────────────────────────────────────────────────────
private val NavyBlue    = Color(0xFF2D3AC1)
private val SoftBg      = Color(0xFFF0F1FB)
private val GoldAccent  = Color(0xFFB87333)
private val TripTint    = Color(0xFFECEEFC)
private val OfferTint   = Color(0xFFFFF5E6)
private val AccountTint = Color(0xFFF5F5F5)

// ── Category helper ───────────────────────────────────────────────────────────
private enum class NotifCategory { TRIPS, OFFERS, ACCOUNT }

private fun Notification.category(): NotifCategory = when (type) {
    "booking", "schedule" -> NotifCategory.TRIPS
    "payment"             -> NotifCategory.ACCOUNT
    "promotion"           -> NotifCategory.OFFERS
    else                  -> NotifCategory.ACCOUNT
}

private fun NotifCategory.label()    = name
private fun NotifCategory.tint()     = when (this) {
    NotifCategory.TRIPS   -> NavyBlue
    NotifCategory.OFFERS  -> GoldAccent
    NotifCategory.ACCOUNT -> Color(0xFF777777)
}
private fun NotifCategory.bgTint()   = when (this) {
    NotifCategory.TRIPS   -> TripTint
    NotifCategory.OFFERS  -> OfferTint
    NotifCategory.ACCOUNT -> AccountTint
}
private fun NotifCategory.icon(): ImageVector = when (this) {
    NotifCategory.TRIPS   -> Icons.Default.DirectionsBus
    NotifCategory.OFFERS  -> Icons.Default.LocalOffer
    NotifCategory.ACCOUNT -> Icons.Default.Person
}

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount   by viewModel.unreadCount.collectAsState()

    // Group by category preserving insertion order
    val grouped = remember(notifications) {
        notifications.groupBy { it.category() }
    }

    Scaffold(
        containerColor = SoftBg,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = NavyBlue
                        )
                    }
                },
                actions = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint     = NavyBlue,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(26.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Page heading ───────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text       = "Notifications",
                                style      = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text  = "Stay updated with your travel journey",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        if (notifications.isNotEmpty()) {
                            Button(
                                onClick  = viewModel::markAllAsRead,
                                shape    = RoundedCornerShape(20.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = NavyBlue.copy(alpha = 0.12f),
                                    contentColor   = NavyBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(
                                    text       = "Mark all as read",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Empty state ────────────────────────────────────────────────
            if (notifications.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint     = NavyBlue.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text  = "No notifications yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF555555)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text  = "We'll let you know when something arrives",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // ── TRIPS ──────────────────────────────────────────────────────
            grouped[NotifCategory.TRIPS]?.let { trips ->
                item {
                    CategoryLabel(
                        label    = "TRIPS",
                        icon     = NotifCategory.TRIPS.icon(),
                        tint     = NotifCategory.TRIPS.tint()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(trips, key = { it.id }) { notif ->
                    NotifCard(
                        notification = notif,
                        bgTint       = NotifCategory.TRIPS.bgTint(),
                        iconBg       = NavyBlue.copy(alpha = 0.12f),
                        iconTint     = NavyBlue,
                        onTap        = { viewModel.markAsRead(notif.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // ── OFFERS ─────────────────────────────────────────────────────
            grouped[NotifCategory.OFFERS]?.let { offers ->
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryLabel(
                        label = "OFFERS",
                        icon  = NotifCategory.OFFERS.icon(),
                        tint  = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(offers, key = { it.id }) { notif ->
                    NotifCard(
                        notification  = notif,
                        bgTint        = NotifCategory.OFFERS.bgTint(),
                        iconBg        = GoldAccent.copy(alpha = 0.15f),
                        iconTint      = GoldAccent,
                        accentBorder  = true,
                        onTap         = { viewModel.markAsRead(notif.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // ── ACCOUNT ────────────────────────────────────────────────────
            grouped[NotifCategory.ACCOUNT]?.let { account ->
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryLabel(
                        label = "ACCOUNT",
                        icon  = NotifCategory.ACCOUNT.icon(),
                        tint  = Color(0xFF777777)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(account, key = { it.id }) { notif ->
                    NotifCard(
                        notification = notif,
                        bgTint       = NotifCategory.ACCOUNT.bgTint(),
                        iconBg       = AccountTint,
                        iconTint     = Color(0xFF555555),
                        onTap        = { viewModel.markAsRead(notif.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// ── Category label ────────────────────────────────────────────────────────────
@Composable
private fun CategoryLabel(
    label: String,
    icon:  ImageVector,
    tint:  Color
) {
    Row(
        modifier          = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text          = label,
            style         = MaterialTheme.typography.labelSmall,
            fontWeight    = FontWeight.Bold,
            color         = tint,
            letterSpacing = 1.sp
        )
    }
}

// ── Notification card ─────────────────────────────────────────────────────────
@Composable
private fun NotifCard(
    notification: Notification,
    bgTint:       Color,
    iconBg:       Color,
    iconTint:     Color,
    accentBorder: Boolean = false,
    onTap:        () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeStr    = remember(notification.createdAt) {
        timeFormat.format(notification.createdAt.toDate())
    }

    val (icon, _) = when (notification.type) {
        "booking"   -> Icons.Default.ConfirmationNumber to iconTint
        "payment"   -> Icons.Default.AccountBalanceWallet to iconTint
        "schedule"  -> Icons.Default.DirectionsBus to iconTint
        "promotion" -> Icons.Default.LocalOffer to iconTint
        "security"  -> Icons.Default.Security to iconTint
        else        -> Icons.Default.Info to iconTint
    }

    val borderMod = if (accentBorder)
        Modifier.border(1.5.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    else
        Modifier

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .then(borderMod)
            .clip(RoundedCornerShape(16.dp))
            .background(bgTint)
            .clickable(onClick = onTap)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint     = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = notification.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        modifier   = Modifier.weight(1f)
                    )
                    Text(
                        text  = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = notification.message,
                    style      = MaterialTheme.typography.bodySmall,
                    color      = Color(0xFF555555),
                    lineHeight = 18.sp
                )
            }

            // Unread dot
            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NavyBlue)
                        .align(Alignment.Top)
                        .offset(y = 4.dp)
                )
            }
        }
    }
}