package com.example.booknpay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departures")
data class Departure(
    @PrimaryKey val departureId: Int,
    val routeId: Int,
    val operatorName: String,
    val seatsAllocatedToSafePass: Int,
    val seatsSoldBySafePass: Int,
    val basePriceMWK: Int
)
