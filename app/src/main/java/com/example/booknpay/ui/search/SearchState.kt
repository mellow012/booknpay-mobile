package com.example.booknpay.ui.search


import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.ScheduleDetails
import java.util.*

data class SearchState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasSearched: Boolean = false,
    val searchQuery: String = "",
    val activeFilter: String = "All",

    // Fields for the Search form/Home transition
    val origin: String = "",
    val destination: String = "",
    val selectedDate: Date = Date(),
    val passengerCount: Int = 1,

    val availableRoutes: List<Route> = emptyList(),
    val allSchedules: List<ScheduleDetails> = emptyList(),
    val searchResults: List<ScheduleDetails> = emptyList(),

    val sortBy: SortOption = SortOption.DEPARTURE_TIME,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..100000f,
    val error: String? = null
)

enum class SortOption {
    DEPARTURE_TIME,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    AVAILABLE_SEATS
}