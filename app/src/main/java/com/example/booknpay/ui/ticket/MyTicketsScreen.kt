package com.example.booknpay.ui.ticket

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.booknpay.data.model.Booking
import java.text.SimpleDateFormat
import java.util.Locale

private val BrandIndigo = Color(0xFF2B3896)
private val BrandBlue   = Color(0xFF0073B7)
private val GreenStatus = Color(0xFF1B8A4E)
private val AmberStatus = Color(0xFFF57F17)

private enum class TripTab { Upcoming, Pending, Confirmed }

// Fake promo images — in production these come from Firebase
private val routeImages = listOf(
    "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600&q=80",
    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600&q=80",
    "https://images.unsplash.com/photo-1499856871958-5b9627545d1a?w=600&q=80",
    "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=600&q=80"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    onNavigateToPayment: (String) -> Unit = {},
    onViewTicket:        (String) -> Unit = {},
    viewModel:           MyTicketsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var activeTab by remember { mutableStateOf(TripTab.Upcoming) }
    var cancelTarget by remember { mutableStateOf<Booking?>(null) }

    cancelTarget?.let { booking ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            icon  = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Cancel booking?") },
            text  = { Text("Cancel ${booking.bookingReference}? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelBooking(booking.id, booking.scheduleId, booking.seatNumbers)
                        cancelTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Cancel booking") }
            },
            dismissButton = { OutlinedButton(onClick = { cancelTarget = null }) { Text("Keep it") } }
        )
    }

    val filteredBookings = when (activeTab) {
        TripTab.Upcoming  -> state.bookings.filter { it.bookingStatus in listOf("confirmed","pending") }
        TripTab.Pending   -> state.bookings.filter { it.bookingStatus == "pending" }
        TripTab.Confirmed -> state.bookings.filter { it.bookingStatus == "confirmed" }
    }

    Column(Modifier.fillMaxSize()) {
        // ── Header ─────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Text("My Trips", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                Icon(Icons.Default.FilterList, "Filter",
                    Modifier.padding(10.dp).size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── Tabs ────────────────────────────────────────────────────────────
        Row(
            Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TripTab.values().forEach { tab ->
                val selected = tab == activeTab
                Surface(
                    onClick  = { activeTab = tab },
                    shape    = RoundedCornerShape(50),
                    color    = if (selected) BrandIndigo else Color.Transparent,
                    border   = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                ) {
                    Text(
                        tab.name,
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
            }
            else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Booking cards
                items(filteredBookings, key = { it.id }) { booking ->
                    TripCard(
                        booking        = booking,
                        imageUrl       = routeImages[booking.id.hashCode().and(0xFF) % routeImages.size],
                        onPayNow       = { onNavigateToPayment(booking.id) },
                        onViewTicket   = { onViewTicket(booking.bookingReference) },
                        onCancelRequest = { cancelTarget = booking }
                    )
                }

                if (filteredBookings.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxWidth().padding(top = 64.dp), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎫", style = MaterialTheme.typography.displayMedium)
                                Spacer(Modifier.height(12.dp))
                                Text("No trips here yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text("Book a bus to get started", style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // "Need a New Destination?" section
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Need a New Destination?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(vertical = 8.dp))
                }

                // Featured route card (image + overlay)
                item {
                    Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp))) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1499856871958-5b9627545d1a?w=800&q=80",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.6f)))
                            )
                        )
                        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            Text("Paris Night Liner", style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Premium Sleeper · From \$39", style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(.8f))
                        }
                    }
                }

                // Member special card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrandIndigo.copy(.08f),
                        border = BorderStroke(1.dp, BrandIndigo.copy(.15f))
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            Arrangement.SpaceBetween, Alignment.CenterVertically
                        ) {
                            Column {
                                Text("MEMBER SPECIAL", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold, color = BrandIndigo,
                                    letterSpacing = 0.8.sp)
                                Text("15% Off Routes", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold, color = BrandIndigo)
                                Text("To Germany & Poland", style = MaterialTheme.typography.bodySmall,
                                    color = BrandIndigo.copy(.65f))
                            }
                            Icon(Icons.Default.LocalOffer, null,
                                tint = BrandIndigo.copy(.3f), modifier = Modifier.size(36.dp))
                        }
                    }
                }

                // Eco transit card
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFE65100).copy(.1f),
                        border = BorderStroke(1.dp, Color(0xFFE65100).copy(.2f))) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            Arrangement.SpaceBetween, Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ECO TRANSIT", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold, color = Color(0xFFE65100),
                                    letterSpacing = 0.8.sp)
                                Text("Green Miles", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold, color = Color(0xFFBF360C))
                                Text("Earn 2x rewards today", style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFBF360C).copy(.65f))
                            }
                            Icon(Icons.Default.Park, null,
                                tint = Color(0xFFE65100).copy(.3f), modifier = Modifier.size(36.dp))
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun TripCard(
    booking:        Booking,
    imageUrl:       String,
    onPayNow:       () -> Unit,
    onViewTicket:   () -> Unit,
    onCancelRequest: () -> Unit
) {
    val isPaid      = booking.paymentStatus == "paid"
    val isConfirmed = booking.bookingStatus == "confirmed"
    val isPending   = booking.bookingStatus == "pending"
    val priceColor  = if (isConfirmed && isPaid) GreenStatus else AmberStatus

    val dateFmt = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val dateStr = booking.bookingDate?.toDate()?.let { dateFmt.format(it) } ?: "—"

    Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp),
        CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            // Hero image
            Box(Modifier.fillMaxWidth().height(150.dp)) {
                AsyncImage(model = imageUrl, contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)))
            }

            Column(Modifier.padding(16.dp)) {
                // Status badge + price
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isConfirmed) GreenStatus else AmberStatus
                    ) {
                        Text(
                            if (isConfirmed) "CONFIRMED" else "WAITING APPROVAL",
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        "MWK ${"%.0f".format(booking.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = priceColor
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Route (use first seat as proxy — real route needs schedule lookup)
                Text(
                    booking.bookingReference.ifBlank { "Route details" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(5.dp))

                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null,
                        Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(5.dp))
                    Text(dateStr, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(.5f))
                Spacer(Modifier.height(12.dp))

                // Seat + Bus ID + Action
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text("Seat", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                booking.seatNumbers.joinToString(", ").ifEmpty { "Pending" },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isConfirmed) {
                            Column {
                                Text("Bus ID", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("TB-${booking.id.takeLast(4).uppercase()}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column {
                                Text("Booking ID", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("#${booking.bookingReference.takeLast(5)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Action
                    when {
                        isConfirmed && isPaid -> Button(
                            onClick = onViewTicket,
                            colors  = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                            shape   = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                        ) { Text("View Ticket", fontWeight = FontWeight.Bold) }

                        isConfirmed && !isPaid -> Button(
                            onClick = onPayNow,
                            colors  = ButtonDefaults.buttonColors(containerColor = GreenStatus),
                            shape   = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                        ) { Text("Pay Now", fontWeight = FontWeight.Bold) }

                        isPending -> Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text("Processing",
                                Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}