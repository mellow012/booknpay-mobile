package com.example.booknpay.ui.booking.components

import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.booknpay.ui.booking.BookingState
import java.text.SimpleDateFormat
import java.util.*

// ── Color tokens ──────────────────────────────────────────────────────────────
private val NavyBlue  = Color(0xFF2D3AC1)
private val DeepNavy  = Color(0xFF1A2070)
private val SoftBg    = Color(0xFFF0F1FB)
private val GreenOk   = Color(0xFF2E7D32)

// ── ConfirmationScreen ────────────────────────────────────────────────────────
// Keeps the exact same signature as before so nothing in BookingScreen.kt breaks.
@Composable
fun ConfirmationScreen(
    state:                  BookingState,
    onSubmitBookingRequest: () -> Unit,
    onBack:                 () -> Unit
) {
    val timeFormat = SimpleDateFormat("EEE, MMM dd 'at' HH:mm", Locale.getDefault())
    val departureTime = state.scheduleDetails?.schedule?.departureDateTime
        ?.toDate()?.let { timeFormat.format(it) } ?: "N/A"

    // Derived amounts
    val pricePerSeat = state.scheduleDetails?.schedule?.price ?: 0.0
    val seatCount    = state.selectedSeats.size
    val platformFee  = 0.0   // extend later if needed
    val total        = state.totalAmount

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(SoftBg),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Page header ────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text       = "Booking Summary",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text  = "Review your details before submitting",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Route confirmation card ────────────────────────────────────────
        item {
            RouteCard(
                origin        = state.scheduleDetails?.origin ?: "",
                destination   = state.scheduleDetails?.destination ?: "",
                departureTime = departureTime
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Important notice ───────────────────────────────────────────────
        item {
            NoticeCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Fare breakdown ─────────────────────────────────────────────────
        item {
            SummaryCard(title = "Fare Breakdown") {
                FareRow(label = "Price per seat",    value = "MWK ${"%,.0f".format(pricePerSeat)}")
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                FareRow(label = "Number of seats",   value = "$seatCount")
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Total Amount",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text       = "MWK ${"%,.0f".format(total)}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = NavyBlue
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text  = "Payment required after operator approval",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Passengers ─────────────────────────────────────────────────────
        item {
            SummaryCard(title = "Passengers") {
                state.passengerDetails.forEachIndexed { i, passenger ->
                    PassengerRow(index = i, passenger = passenger)
                    if (i < state.passengerDetails.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Action buttons ─────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape    = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick  = onSubmitBookingRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape    = RoundedCornerShape(28.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Submit Request", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ── Route Card ────────────────────────────────────────────────────────────────
@Composable
private fun RouteCard(
    origin:        String,
    destination:   String,
    departureTime: String
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colors = listOf(DeepNavy, NavyBlue)))
            .padding(20.dp)
    ) {
        Column {
            Text(
                text          = "ROUTE CONFIRMATION",
                style         = MaterialTheme.typography.labelSmall,
                color         = Color.White.copy(alpha = 0.65f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = origin,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                    Text(
                        text  = "Origin",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                // Arrow with bus icon
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsBus,
                            null,
                            tint     = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = destination,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                    Text(
                        text  = "Destination",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    null,
                    tint     = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text  = departureTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ── Notice Card ───────────────────────────────────────────────────────────────
@Composable
private fun NoticeCard() {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavyBlue.copy(alpha = 0.08f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.Info,
            null,
            tint     = NavyBlue,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text       = "Booking Request Process",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = NavyBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = "Your booking will be sent to the bus operator for approval. " +
                        "Payment is only required once they accept your request.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF444466),
                lineHeight = 18.sp
            )
        }
    }
}

// ── Generic Summary Card ──────────────────────────────────────────────────────
@Composable
private fun SummaryCard(
    title:   String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

// ── Fare Row ──────────────────────────────────────────────────────────────────
@Composable
private fun FareRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Passenger Row ─────────────────────────────────────────────────────────────
@Composable
private fun PassengerRow(
    index:     Int,
    passenger: com.example.booknpay.ui.booking.PassengerDetail
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SoftBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint     = NavyBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text       = passenger.name.ifBlank { "Passenger ${index + 1}" },
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = "${passenger.age} yrs • ${passenger.gender.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = NavyBlue.copy(alpha = 0.1f)
        ) {
            Text(
                text     = "Seat ${passenger.seatNumber}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style    = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color    = NavyBlue
            )
        }
    }
}