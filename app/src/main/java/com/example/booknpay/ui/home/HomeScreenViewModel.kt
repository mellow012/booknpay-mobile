package com.example.booknpay.ui.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.data.repository.FirebaseBookingRepository
import com.example.booknpay.data.repository.FirebaseRouteRepository
import com.example.booknpay.data.repository.FirebaseScheduleRepository
import com.example.booknpay.domain.repository.AuthRepository
import com.example.booknpay.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val notificationCount: Int = 0,
    val upcomingBookings: List<UpcomingBooking> = emptyList(),
    val availableSchedules: List<ScheduleDetails> = emptyList(),
    val popularRoutes: List<PopularRoute> = listOf(
        PopularRoute("Lilongwe", "Blantyre", "🏙️", 45),
        PopularRoute("Blantyre", "Mzuzu", "🌄", 32),
        PopularRoute("Lilongwe", "Mzuzu", "🌳", 28),
        PopularRoute("Zomba", "Lilongwe", "⛰️", 24)
    ),
    val error: String? = null
)

data class UpcomingBooking(
    val id: String,
    val route: String,
    val date: String,
    val time: String,
    val seatNumbers: String,
    val status: String
)

data class PopularRoute(
    val origin: String,
    val destination: String,
    val emoji: String,
    val tripCount: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val scheduleRepository: FirebaseScheduleRepository,
    private val bookingRepository: FirebaseBookingRepository,
    private val notificationRepository: NotificationRepository,
    private val routeRepository: FirebaseRouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // 1. Load user name (one-time)
        viewModelScope.launch {
            loadUserName()
        }

        // 2. Load real-time schedules + routes (this is the main flow)
        viewModelScope.launch {
            loadSchedulesAndRoutes()
        }

        // 3. Load upcoming bookings
        viewModelScope.launch {
            loadBookings()
        }

        // 4. Load notification count
        viewModelScope.launch {
            loadNotifications()
        }
    }

    private suspend fun loadUserName() {
        authRepository.getCurrentUser().firstOrNull()?.let { user ->
            val profile = authRepository.getUserProfile(user.uid)
            if (profile is AuthResult.Success) {
                _state.update { it.copy(userName = profile.data.firstName) }
            }
        }
    }

    /**
     * Core real-time data loading for schedules
     * Loads routes once, then listens to schedule changes forever
     */
    private fun loadSchedulesAndRoutes() {
        viewModelScope.launch {
            // Step 1: Load routes once (suspend call)
            val routes = try {
                val fetched = routeRepository.getRoutes()
                println("DEBUG HomeVM: Successfully fetched ${fetched.size} routes from repository")
                fetched.forEach { r ->
                    println("DEBUG Route: ID=${r.id} | Origin=${r.origin} | Dest=${r.destination} | Active=${r.isActive}")
                }
                fetched
            } catch (e: Exception) {
                println("DEBUG HomeVM: Failed to load routes: ${e.message}")
                emptyList<Route>()
            }

            val routeMap = routes.associateBy { it.id }
            println("DEBUG HomeVM: routeMap size = ${routeMap.size} | Keys = ${routeMap.keys.joinToString()}")

            // Step 2: Start real-time listening to schedules
            scheduleRepository.getSchedulesRealtime()
                .catch { e ->
                    println("DEBUG HomeVM: Real-time schedules error: ${e.message}")
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .onEach { schedules ->
                    println("DEBUG HomeVM: Received ${schedules.size} schedules from Firestore")
                }
                .collect { schedules ->
                    val now = Date()
                    val details = schedules
                        .filter { schedule ->
                            val depDate = schedule.departureDateTime?.toDate()
                            val isFuture = depDate?.after(now) == true

                            println("DEBUG Schedule ${schedule.id}:")
                            println("  Departure raw: ${schedule.departureDateTime}")
                            println("  Parsed Date: $depDate")
                            println("  Current now: $now")
                            println("  Is future? $isFuture")

                            isFuture
                        }
                        .mapNotNull { schedule ->
                            val route = routeMap[schedule.routeId]
                            if (route == null) {
                                println("DEBUG NO MATCH - No route found for routeId: ${schedule.routeId}")
                                println("DEBUG Available route IDs: ${routeMap.keys.joinToString()}")
                            } else {
                                println("DEBUG MATCH FOUND - routeId=${schedule.routeId} → ${route.origin} to ${route.destination}")
                            }
                            route?.let { r ->
                                ScheduleDetails(
                                    schedule = schedule,
                                    origin = r.origin,
                                    destination = r.destination
                                )
                            }
                        }
                        .sortedBy { it.schedule.departureDateTime?.toDate() }

                    _state.update {
                        it.copy(
                            availableSchedules = details,
                            isLoading = false
                        )
                    }

                    println("DEBUG HomeVM: Updated UI with ${details.size} upcoming schedule details")
                }
        }
    }

    private suspend fun loadBookings() {
        bookingRepository.getUserBookingsRealtime()
            .catch { e -> println("Bookings error: ${e.message}") }
            .collect { bookings ->
                // Your existing logic to build upcomingList...
                // (keep it as-is, just add println("Loaded ${bookings.size} bookings") for debug)
            }
    }

    private suspend fun loadNotifications() {
        notificationRepository.getUserNotificationsRealtime()
            .catch { e -> println("Notifications error: ${e.message}") }
            .collect { notifications ->
                val unread = notifications.count { !it.isRead }
                _state.update { it.copy(notificationCount = unread) }
            }
    }
    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return format.format(date)
    }

    private fun formatTime(date: Date): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }
}