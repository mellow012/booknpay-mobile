package com.example.booknpay.ui.route

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.data.repository.FirebaseRouteRepository
import com.example.booknpay.data.repository.FirebaseScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class RouteSchedulesState(
    val isLoading: Boolean = true,
    val origin: String = "",
    val destination: String = "",
    val schedules: List<ScheduleDetails> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RouteSchedulesViewModel @Inject constructor(
    private val scheduleRepository: FirebaseScheduleRepository,
    private val routeRepository: FirebaseRouteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RouteSchedulesState())
    val state: StateFlow<RouteSchedulesState> = _state.asStateFlow()

    init {
        // Args are stored with spaces replaced by underscores in the nav route
        val rawOrigin      = savedStateHandle.get<String>("origin")?.replace("_", " ") ?: ""
        val rawDestination = savedStateHandle.get<String>("destination")?.replace("_", " ") ?: ""
        _state.update { it.copy(origin = rawOrigin, destination = rawDestination) }
        loadSchedules(rawOrigin, rawDestination)
    }

    private fun loadSchedules(origin: String, destination: String) {
        viewModelScope.launch {
            val routes: List<Route> = try { routeRepository.getRoutes() } catch (e: Exception) { emptyList() }
            val routeMap = routes.associateBy { it.id }

            scheduleRepository.getSchedulesRealtime()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .map { schedules ->
                    val now = Date()
                    schedules
                        .filter { sch ->
                            val depDate = sch.departureDateTime?.toDate()
                            depDate?.after(now) == true
                        }
                        .mapNotNull { sch ->
                            routeMap[sch.routeId]?.let { r ->
                                if (r.origin.equals(origin, ignoreCase = true) &&
                                    r.destination.equals(destination, ignoreCase = true)) {
                                    ScheduleDetails(schedule = sch, origin = r.origin, destination = r.destination)
                                } else null
                            }
                        }
                        .sortedBy { it.schedule.departureDateTime?.toDate() }
                }
                .collect { result ->
                    _state.update { it.copy(isLoading = false, schedules = result, error = null) }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}