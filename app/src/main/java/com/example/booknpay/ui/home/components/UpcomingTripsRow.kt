package com.example.booknpay.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknpay.ui.home.UpcomingBooking

@Composable
fun UpcomingTripsRow(
    bookings: List<UpcomingBooking>,
    onBookingClick: (String) -> Unit
) {
    if (bookings.isEmpty()) return
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding        = PaddingValues(horizontal = 16.dp)
    ) {
        items(bookings, key = { it.id }) { booking ->
            UpcomingTripCard(booking = booking, onClick = { onBookingClick(booking.id) })
        }
    }
}

@Composable
private fun UpcomingTripCard(
    booking: UpcomingBooking,
    onClick: () -> Unit
) {
    // Colours keyed to booking status
    val (cardColor, accentColor) = when {
        booking.status == "confirmed" && booking.paymentStatus == "paid" ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        booking.status == "confirmed" ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        else ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
    }

    val (statusIcon, statusLabel) = when {
        booking.status == "confirmed" && booking.paymentStatus == "paid" ->
            Icons.Default.CheckCircle to "Confirmed"
        booking.status == "confirmed" ->
            Icons.Default.Payment to "Pay now"
        else ->
            Icons.Default.HourglassEmpty to "Pending"
    }

    Card(
        modifier  = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Status badge
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(statusIcon, null, tint = accentColor, modifier = Modifier.size(14.dp))
                    Text(
                        text       = statusLabel,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor
                    )
                }
                Text(
                    text  = booking.bookingReference,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Route: Origin → Destination
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text  = "From",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    Text(
                        text       = booking.origin,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint     = accentColor,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .size(20.dp)
                )
                Column {
                    Text(
                        text  = "To",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    Text(
                        text       = booking.destination,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = accentColor.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Date + time row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TripInfoChip(
                    icon  = Icons.Default.CalendarToday,
                    label = booking.departureDate,
                    tint  = accentColor
                )
                TripInfoChip(
                    icon  = Icons.Default.AccessTime,
                    label = booking.departureTime,
                    tint  = accentColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seats + amount row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TripInfoChip(
                    icon  = Icons.Default.EventSeat,
                    label = "Seats: ${booking.seatNumbers}",
                    tint  = accentColor
                )
                Text(
                    text       = "MWK ${"%,.0f".format(booking.totalAmount)}",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
            }
        }
    }
}

@Composable
private fun TripInfoChip(
    icon:  androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint:  androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = tint.copy(alpha = 0.75f), modifier = Modifier.size(13.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
    }
}