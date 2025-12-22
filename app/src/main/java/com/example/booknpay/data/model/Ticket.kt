package com.example.booknpay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val ticketId: Int = 0,
    val bookingId: String,
    val departureId: Int,
    val passengerName: String,
    val passengerAge: Int,
    val passengerSex: String,
    val passengerPhone: String,
    val numberOfPassengers: Int,
    val paymentStatus: String,
    val syncStatus: String
)
