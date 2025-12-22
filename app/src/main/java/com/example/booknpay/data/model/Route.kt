package com.example.booknpay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class Route(
    @PrimaryKey val routeId: Int,
    val originCity: String,
    val destinationCity: String
)
