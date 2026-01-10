package com.example.booknpay.ui.search

import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.ScheduleDetails
import java.util.*

data class SearchState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val origin: String = "",
    val destination: String = "",
    val selectedDate: Date = Date(),
    val availableRoutes: List<Route> = emptyList(),
    val searchResults: List<ScheduleDetails> = emptyList(),
    val hasSearched: Boolean = false,
    val error: String? = null,
    val sortBy: SortOption = SortOption.DEPARTURE_TIME,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..100000f
)

enum class SortOption {
    DEPARTURE_TIME,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    AVAILABLE_SEATS
}