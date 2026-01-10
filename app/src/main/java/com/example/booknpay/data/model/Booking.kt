package com.example.booknpay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String = "", // Using Firestore document ID as the unique primary key
    val bookingReference: String = "",
    val bookingStatus: String = "",
    val companyId: String = "",
    val scheduleId: String = "",
    val userId: String = "",
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "",
    val paymentStatus: String = "",
    val seatNumbers: List<String> = emptyList(), // Will be handled by TypeConverter
    val bookingDate: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val passengerDetails: List<Passenger> = emptyList() // Will be handled by TypeConverter
)

@IgnoreExtraProperties
data class Passenger(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val seatNumber: String? = null
)
