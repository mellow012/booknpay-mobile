package com.example.booknpay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey val id: String = "",
    val busId: String = "",
    val routeId: String = "",
    val companyId: String = "",
    val departureDateTime: Timestamp? = null,
    val arrivalDateTime: Timestamp? = null,
    val price: Double = 0.0,
    val availableSeats: Int = 0,
    val bookedSeats: List<String> = emptyList(), // Added this line
    val isActive: Boolean = true,
    val status: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
