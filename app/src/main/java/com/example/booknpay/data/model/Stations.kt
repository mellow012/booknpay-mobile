package com.example.booknpay.data.model

data class Station(
    val id: String = "",
    val name: String = "",
    val city: String = "", // e.g., "Lilongwe", "Blantyre"
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String? = null,
    val facilities: List<String> = emptyList(), // e.g., ["Toilets", "ATM", "Cafe"]
    val operatingCompanies: List<String> = emptyList() // IDs of BusCompany
)