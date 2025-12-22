package com.example.booknpay.data.model

import androidx.room.ColumnInfo

/**
 * A data class to hold the combined details of a ticket for display purposes.
 * This is created by joining the Ticket, Departure, and Route tables.
 */
data class TicketDetails(
    val bookingId: String,
    val passengerName: String,
    val paymentStatus: String,
    // We use @ColumnInfo because Room's query result column names might differ
    @ColumnInfo(name = "origin_city") val originCity: String,
    @ColumnInfo(name = "destination_city") val destinationCity: String,
    @ColumnInfo(name = "operator_name") val operatorName: String
)
