package com.example.booknpay.ui.profile

import com.example.booknpay.data.model.User

data class ProfileState(
    val user: User? = null,
    val totalTrips: Int = 0,
    val upcomingTrips: Int = 0,
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val loyaltyPts: Int     = 0
)