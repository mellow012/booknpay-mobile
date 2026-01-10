package com.example.booknpay.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class User(
    @DocumentId
    val id: String = "", // Removed @Exclude so Firestore can map the Document ID here
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
    val setupCompleted: Boolean = false
) {
    // No-argument constructor required by Firestore (satisfied by default values)
    fun getFullName(): String = "$firstName $lastName".trim()
}

// For registration
