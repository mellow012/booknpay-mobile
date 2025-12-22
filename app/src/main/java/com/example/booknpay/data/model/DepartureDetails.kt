package com.example.booknpay.data.model

import androidx.room.Embedded

/**
 * A data class to hold the combined details of a departure and its route.
 */
data class DepartureDetails(
    // By embedding the Departure, we get all its fields automatically.
    @Embedded
    val departure: Departure,

    val originCity: String,
    val destinationCity: String
)
