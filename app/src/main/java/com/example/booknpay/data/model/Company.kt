package com.example.booknpay.data.model

data class Company(
    val id: String = "",
    val name: String = "",
    val logo: String? = null, // This is your Base64 string
    val description: String = "",
    val contact: String = "",
    val status: String = "active",
    val branches: List<String> = emptyList(),
    // We add a default color for "Quantum Tours" branding
    val themeColor: String = "#0073B7",
    val totalRoutes: Int = 0,
    val totalBuses: Int = 0
)
//"Route & Fare" model
data class CompanyService(
    val id: String = "",
    val origin: String = "",
    val destination: String = "",
    val adultFare: Double = 0.0,
    val childFare: Double = 0.0,
    val departureTimes: List<String> = emptyList(), // e.g., ["07:30", "13:00"]
    val busType: String = "Standard" // Executive, Semi-Luxury, etc.
)