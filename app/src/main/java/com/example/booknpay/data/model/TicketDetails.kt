package com.example.booknpay.data.model

/**
 * Combined ticket details for display in My Tickets screen
 * Built by combining Booking, Schedule, and Route data in the repository
 * DO NOT use @Embedded or @Entity - this is a pure data class
 */
data class TicketDetails(
    val booking: Booking,
    val originCity: String,
    val destinationCity: String,
    val operatorName: String,
    val departureTime: String,
    val arrivalTime: String
)