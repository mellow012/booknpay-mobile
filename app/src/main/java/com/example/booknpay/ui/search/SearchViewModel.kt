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
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // 1. Fetch everything in parallel
                val routes = routeRepository.getRoutes()
                val rawSchedules = scheduleRepository.getSchedules() // This returns List<Schedule>
                // If you have a company repository, fetch that too.
                // Otherwise, we map what we have.

                // 2. Map List<Schedule> -> List<ScheduleDetails>
                val routeMap = routes.associateBy { it.id }

                val mappedSchedules = rawSchedules.mapNotNull { schedule ->
                    val route = routeMap[schedule.routeId]
                    if (route != null) {
                        ScheduleDetails(
                            schedule = schedule,
                            origin = route.origin,
                            destination = route.destination,
                            // If your ScheduleDetails model has a company field:
                            // company = companyMap[schedule.companyId]
                        )
                    } else null
                }

                _state.update {
                    it.copy(
                        availableRoutes = routes,
                        allSchedules = mappedSchedules, // Now types match!
                        isLoading = false
                    )
                }

                // Trigger the initial filter to show data immediately
                applyFiltersAndSort()

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Connection error: ${e.localizedMessage}") }
            }
        }
    }

    fun onFilterChange(filter: String) {
        _state.update { it.copy(activeFilter = filter) }
        applyFiltersAndSort() // Re-run the search logic with the new filter
    }
    // --- Search Logic ---

    fun onQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query, hasSearched = query.isNotEmpty()) }
        applyFiltersAndSort()
    }

    fun onOriginChange(origin: String) {
        _state.update { it.copy(origin = origin) }
    }

    fun onDestinationChange(dest: String) {
        _state.update { it.copy(destination = dest) }
    }

    fun onDateChange(date: Date) {
        _state.update { it.copy(selectedDate = date) }
    }

    fun onPassengerCountChange(count: Int) {
        _state.update { it.copy(passengerCount = count.coerceIn(1, 20)) }
    }

    // --- Filter & Sort Logic ---

    fun onSortChange(option: SortOption) {
        _state.update { it.copy(sortBy = option) }
        applyFiltersAndSort()
    }

    fun onPriceRangeChange(range: ClosedFloatingPointRange<Float>) {
        _state.update { it.copy(priceRange = range) }
        applyFiltersAndSort()
    }

    fun searchSchedules() {
        if (_state.value.origin.isEmpty() || _state.value.destination.isEmpty()) {
            _state.update { it.copy(error = "Please select both From and To cities.") }
            return
        }
        _state.update { it.copy(hasSearched = true) }
        applyFiltersAndSort()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            loadInitialData()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * This is the "Engine" that handles both global text search
     * and specific Route/Date filtering.
     */
    private fun applyFiltersAndSort() {
        val currentState = _state.value
        val query = currentState.searchQuery.lowercase().trim()

        var filtered = currentState.allSchedules.filter { item ->
            val matchesPrice = item.schedule.price.toFloat() in currentState.priceRange
            val matchesPassengers = item.schedule.availableSeats >= currentState.passengerCount

            // If user typed in the Global Search Bar
            val matchesQuery = if (query.isEmpty()) true else {
                item.origin.lowercase().contains(query) ||
                        item.destination.lowercase().contains(query) ||
                        (item.company?.name?.lowercase()?.contains(query) ?: false)
            }

            // If user used the Specific Route Picker (Integrated Search)
            val matchesRoute = if (currentState.origin.isEmpty() || currentState.destination.isEmpty()) true else {
                item.origin.equals(currentState.origin, ignoreCase = true) &&
                        item.destination.equals(currentState.destination, ignoreCase = true)
            }

            matchesPrice && matchesPassengers && matchesQuery && matchesRoute
        }

        // Apply Sorting
        filtered = when (currentState.sortBy) {
            SortOption.DEPARTURE_TIME -> filtered.sortedBy { it.schedule.departureDateTime }
            SortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.schedule.price }
            SortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.schedule.price }
            SortOption.AVAILABLE_SEATS -> filtered.sortedByDescending { it.schedule.availableSeats }
        }

        _state.update { it.copy(searchResults = filtered) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}