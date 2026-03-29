package com.example.booknpay.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class User(

    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val role: String = "customer",
    val passwordSet: Boolean = false,

    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @ServerTimestamp
    val updatedAt: Timestamp? = null,

    val currentAddress: String? = null,
    val sex: String? = null,
    val nationalId: String? = null,
    val companyId: String? = null,
    val setupCompleted: Boolean = false,
    val emailVerified: Boolean = false // Added to match Firestore fields
) {
    // This helper makes it easy to display names in your Compose UI
    @get:Exclude
    val fullName: String
        get() = if (firstName.isBlank() && lastName.isBlank()) "Guest User"
        else "$firstName $lastName".trim()
}