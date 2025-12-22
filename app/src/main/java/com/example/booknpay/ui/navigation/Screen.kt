package com.example.booknpay.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector?) {
    object Home : Screen("home_route", "Home", Icons.Default.Home)
    object Booking : Screen("booking_route", "Booking", Icons.Default.BookOnline)
    object MyTickets : Screen("my_tickets_route", "My Tickets", Icons.Default.ConfirmationNumber)
    object Notifications : Screen("notifications_route", "Notifications", Icons.Default.Notifications)

    // Screen for the confirmation page
    object Confirmation : Screen("confirmation_route", "Confirm Booking", null)

    // Screen for the individual e-ticket. It does not appear in the bottom bar.
    object Ticket : Screen("ticket_route/{bookingId}/{passengerName}", "E-Ticket", null) {
        fun createRoute(bookingId: String, passengerName: String) = "ticket_route/$bookingId/$passengerName"
    }
}
