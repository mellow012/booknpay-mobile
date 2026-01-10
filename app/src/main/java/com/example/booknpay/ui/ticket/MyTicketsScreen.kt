package com.example.booknpay.ui.ticket


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booknpay.data.model.Booking

@Composable
fun MyTicketsScreen(
    viewModel: MyTicketsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Bookings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${state.bookings.size} booking(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.bookings.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No bookings yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Your booking requests will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.bookings) { booking ->
                        BookingCard(booking = booking)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (booking.bookingStatus) {
                "confirmed" -> MaterialTheme.colorScheme.primaryContainer
                "pending" -> MaterialTheme.colorScheme.secondaryContainer
                "rejected" -> MaterialTheme.colorScheme.errorContainer
                "cancelled" -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status and Reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookingStatusChip(
                    status = booking.bookingStatus,
                    paymentStatus = booking.paymentStatus
                )

                Text(
                    text = booking.bookingReference,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Passenger Names
            booking.passengerDetails.forEachIndexed { index, passenger ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${passenger.name} (Seat ${passenger.seatNumber})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "MWK ${booking.totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Action button based on status
            Spacer(modifier = Modifier.height(12.dp))

            when {
                booking.bookingStatus == "confirmed" && booking.paymentStatus == "pending" -> {
                    Button(
                        onClick = { /* Navigate to payment */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Payment, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pay Now")
                    }
                }
                booking.bookingStatus == "pending" -> {
                    OutlinedButton(
                        onClick = { /* Cancel booking */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Request")
                    }
                }
                booking.bookingStatus == "confirmed" && booking.paymentStatus == "paid" -> {
                    OutlinedButton(
                        onClick = { /* View ticket */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Ticket")
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingStatusChip(status: String, paymentStatus: String) {
    val (icon, text, color) = when {
        status == "confirmed" && paymentStatus == "paid" -> Triple(
            Icons.Default.CheckCircle,
            "Confirmed",
            MaterialTheme.colorScheme.primary
        )
        status == "confirmed" && paymentStatus == "pending" -> Triple(
            Icons.Default.Payment,
            "Awaiting Payment",
            MaterialTheme.colorScheme.tertiary
        )
        status == "pending" -> Triple(
            Icons.Default.HourglassEmpty,
            "Pending Approval",
            MaterialTheme.colorScheme.secondary
        )
        status == "rejected" -> Triple(
            Icons.Default.Cancel,
            "Rejected",
            MaterialTheme.colorScheme.error
        )
        status == "cancelled" -> Triple(
            Icons.Default.Cancel,
            "Cancelled",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Triple(
            Icons.Default.Info,
            status.replaceFirstChar { it.uppercase() },
            MaterialTheme.colorScheme.onSurface
        )
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}