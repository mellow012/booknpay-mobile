package com.example.booknpay.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.data.repository.FirebaseRouteRepository
import com.example.booknpay.data.repository.FirebaseScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val scheduleRepository: FirebaseScheduleRepository,
    private val routeRepository: FirebaseRouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    init {
        loadRoutes()
    }

    private fun loadRoutes() {
        viewModelScope.launch {
            try {
                val routes = routeRepository.getRoutes()
                _state.update { it.copy(availableRoutes = routes) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load routes") }
            }
        }
    }

    fun onOriginChange(origin: String) {
        _state.update { it.copy(origin = origin) }
    }

    fun onDestinationChange(destination: String) {
        _state.update { it.copy(destination = destination) }
    }

    fun onDateChange(date: Date) {
        _state.update { it.copy(selectedDate = date) }
    }

    fun onSortChange(sortOption: SortOption) {
        _state.update {
            it.copy(
                sortBy = sortOption,
                searchResults = sortResults(it.searchResults, sortOption)
            )
        }
    }

    fun onPriceRangeChange(range: ClosedFloatingPointRange<Float>) {
        _state.update { it.copy(priceRange = range) }
        filterResults()
    }

    fun searchSchedules() {
        if (_state.value.origin.isEmpty() || _state.value.destination.isEmpty()) {
            _state.update { it.copy(error = "Please select origin and destination") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, hasSearched = true) }

            try {
                combine(
                    scheduleRepository.getSchedulesRealtime(),
                    flow { emit(routeRepository.getRoutes()) }
                ) { schedules, routes ->
                    val routeMap = routes.associateBy { it.id }

                    // Filter schedules by origin/destination and date
                    val filtered = schedules.mapNotNull { schedule ->
                        val route = routeMap[schedule.routeId]
                        if (route != null &&
                            route.origin.equals(_state.value.origin, ignoreCase = true) &&
                            route.destination.equals(_state.value.destination, ignoreCase = true) &&
                            isSameDay(schedule.departureDateTime?.toDate(), _state.value.selectedDate)) {
                            ScheduleDetails(schedule, route.origin, route.destination)
                        } else null
                    }

                    filtered
                }
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Search failed: ${e.message}"
                            )
                        }
                    }
                    .collect { results ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                searchResults = sortResults(results, it.sortBy)
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Search failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            searchSchedules()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun filterResults() {
        val current = _state.value
        val filtered = current.searchResults.filter {
            it.schedule.price in current.priceRange.start.toDouble()..current.priceRange.endInclusive.toDouble()
        }
        _state.update { it.copy(searchResults = sortResults(filtered, it.sortBy)) }
    }

    private fun sortResults(results: List<ScheduleDetails>, sortOption: SortOption): List<ScheduleDetails> {
        return when (sortOption) {
            SortOption.DEPARTURE_TIME -> results.sortedBy { it.schedule.departureDateTime }
            SortOption.PRICE_LOW_TO_HIGH -> results.sortedBy { it.schedule.price }
            SortOption.PRICE_HIGH_TO_LOW -> results.sortedByDescending { it.schedule.price }
            SortOption.AVAILABLE_SEATS -> results.sortedByDescending { it.schedule.availableSeats }
        }
    }

    private fun isSameDay(date1: Date?, date2: Date): Boolean {
        if (date1 == null) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}