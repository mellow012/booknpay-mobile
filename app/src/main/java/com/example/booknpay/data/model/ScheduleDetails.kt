package com.example.booknpay.data.model


/**
 * A data class to hold the combined details of a schedule and its route for the UI.
 */
data class ScheduleDetails(
    // By embedding the Schedule, we get all its fields automatically.
    val schedule: Schedule,
    val origin: String,
    val destination: String
)
