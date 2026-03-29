package com.example.booknpay.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AirplaneTicket
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.model.Company
import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.Schedule
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.data.repository.FirebaseBookingRepository
import com.example.booknpay.data.repository.FirebaseCompanyRepository
import com.example.booknpay.data.repository.FirebaseRouteRepository
import com.example.booknpay.data.repository.FirebaseScheduleRepository
import com.example.booknpay.domain.repository.AuthRepository
import com.example.booknpay.domain.repository.NotificationRepository
import com.example.booknpay.util.Converters
import com.example.booknpay.util.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ─── UI Models ─────────────────────────────────────────────────────────────────

enum class ActionType { BOOK_BUS, MY_TICKETS, SCHEDULES, WALLET,PARCELS,HELP }

data class QuickActionModel(
    val title: String,
    val icon: ImageVector,
    val actionType: ActionType
)

data class HomeState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userInitials: String = "",
    val notificationCount: Int = 0,
    val currentLocation: String? = "Select Location",
    val isLocating: Boolean = false,
    val selectedOrigin: String? = null,
    val selectedDestination: String? = null,
    val selectedDateMillis: Long? = null,
    val formattedDate: String = "Select Date",
    val passengerCount: Int = 1,
    val searchResults: List<ScheduleDetails> = emptyList(),
    val isSearching: Boolean = false,
    val boardingSoon:List<ScheduleDetails> = emptyList(),
    val featuredTrip: UpcomingBooking? = null,
    val availableSchedules: List<ScheduleDetails> = emptyList(),
    val availableDestinations: List<String> = emptyList(),
    val availableOrigins: List<String> = emptyList(),
    val popularBuses: List<PopularBus> = emptyList(),
    val activeRoutes: List<ActiveRoute> = emptyList(),
    val allSchedules: List<ScheduleDetails> = emptyList(),
    val quickActions: List<QuickActionModel> = emptyList(),
    val error: String? = null
)

data class UpcomingBooking(
    val id: String,
    val bookingReference: String,
    val origin: String,
    val destination: String,
    val departureDate: String,
    val departureTime: String,
    val departureDateMillis: Long?,
    val seatNumbers: String,
    val status: String,
    val paymentStatus: String,
    val totalAmount: Double
)

data class PopularBus(
    val scheduleId: String,
    val companyName: String?,
    val companyId: String,
    val busId: String,
    val companyLogoUrl: String?,
    val origin: String,
    val destination: String,
    val departureTime: String,
    val arrivalTime: String,
    val price: Double,
    val seatsLeft: Int,
    val totalSeats: Int
)

data class ActiveRoute(
    val origin: String,
    val destination: String,
    val scheduleCount: Int,
    val minPrice: Double,
    val nextDeparture: String,
    val emoji: String
)

// ─── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val scheduleRepository: FirebaseScheduleRepository,
    private val bookingRepository: FirebaseBookingRepository,
    private val notificationRepository: NotificationRepository,
    private val routeRepository: FirebaseRouteRepository,
    private val companyRepository: FirebaseCompanyRepository,
    private val locationService: LocationService,) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        _state.update { it.copy(quickActions = getQuickActions()) }
        loadUserName()
        loadCombinedData()
        loadNotifications()
    }

    private fun getQuickActions() = listOf(
        QuickActionModel("Discover", Icons.Default.DirectionsBus, ActionType.BOOK_BUS),
        QuickActionModel("Tickets", Icons.AutoMirrored.Filled.AirplaneTicket, ActionType.MY_TICKETS),
        QuickActionModel("Schedules", Icons.Default.CalendarToday, ActionType.SCHEDULES),
        QuickActionModel("Wallet", Icons.Default.AccountBalanceWallet, ActionType.WALLET)
    )

    private fun loadUserName() {
        viewModelScope.launch {
            authRepository.getCurrentUser().firstOrNull()?.let { user ->
                val profile = authRepository.getUserProfile(user.uid)
                if (profile is AuthResult.Success) {
                    _state.update { currentState ->
                        currentState.copy(
                            userName = profile.data.firstName,
                            userInitials = buildInitials(profile.data.firstName, profile.data.lastName)
                        )
                    }
                }
            }
        }
    }

    private fun loadCombinedData() {
        viewModelScope.launch {
            try {
                // Fetch static/master data once to use in the stream
                val companies = companyRepository.getCompanies()
                val routes = routeRepository.getRoutes()

                val routeMap = routes.associateBy { it.id }
                val companyMap = companies.associateBy { it.id }

                combine(
                    scheduleRepository.getSchedulesRealtime(),
                    bookingRepository.getUserBookingsRealtime()
                ) { schedules, bookings ->
                    val now = Date()
                    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val dateFmt = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())

                    val transitThreshold = Calendar.getInstance().apply { add(Calendar.HOUR, -6) }.time
                    val threeHoursFromNow = Calendar.getInstance().apply { add(Calendar.HOUR, 3) }.time

                    // 1. Join Everything
                    val allScheduleDetails = schedules
                        .filter { it.departureDateTime?.toDate()?.after(transitThreshold) == true }
                        .mapNotNull { sch ->
                            val route = routeMap[sch.routeId]
                            val company = companyMap[sch.companyId]
                            if (route != null) {
                                ScheduleDetails(
                                    schedule = sch,
                                    origin = route.origin,
                                    destination = route.destination,
                                    company = company
                                )
                            } else null
                        }
                        .sortedBy { it.schedule.departureDateTime?.toDate() }

                    // 2. Filter Boarding Soon
                    val boardingSoonModel = allScheduleDetails.filter { sd ->
                        val depTime = sd.schedule.departureDateTime?.toDate()
                        depTime != null && depTime.after(now) &&
                                depTime.before(threeHoursFromNow) && sd.schedule.availableSeats > 0
                    }

                    // 3. Map Popular Buses
                    val popularBusesModel = allScheduleDetails
                        .sortedByDescending { it.schedule.bookedSeats.size }
                        .take(6)
                        .map { sd ->
                            PopularBus(
                                scheduleId = sd.schedule.id,
                                companyName=sd.company?.name,
                                companyId = sd.schedule.companyId,
                                busId = sd.schedule.busId,
                                companyLogoUrl = sd.company?.logo, // The Base64 String
                                origin = sd.origin,
                                destination = sd.destination,
                                departureTime = sd.schedule.departureDateTime?.toDate()?.let { timeFmt.format(it) } ?: "—",
                                arrivalTime = sd.schedule.arrivalDateTime?.toDate()?.let { timeFmt.format(it) } ?: "—",
                                price = sd.schedule.price,
                                seatsLeft = sd.schedule.availableSeats,
                                totalSeats = sd.schedule.availableSeats + sd.schedule.bookedSeats.size
                            )
                        }

                    // 4. Map Active Routes
                    val routeGroups = allScheduleDetails.groupBy { Pair(it.origin, it.destination) }
                    val activeRoutesModel = routeGroups.entries
                        .sortedByDescending { it.value.size }
                        .take(8)
                        .map { (routePair, items) ->
                            val (ori, dest) = routePair
                            val soonest = items.minByOrNull { it.schedule.departureDateTime?.seconds ?: 0L }
                            val depDate = soonest?.schedule?.departureDateTime?.toDate()
                            val nextLabel = depDate?.let {
                                if (isSameDay(it, now)) "Today · ${timeFmt.format(it)}"
                                else "${dateFmt.format(it)} · ${timeFmt.format(it)}"
                            } ?: "—"
                            ActiveRoute(ori, dest, items.size, items.minOf { it.schedule.price }, nextLabel, routeEmoji(ori, dest))
                        }

                    // 5. Map Upcoming Bookings
                    val scheduleMap = schedules.associateBy { it.id }
                    val upcomingBookings = bookings
                        .filter { it.bookingStatus in listOf("confirmed", "pending") }
                        .sortedBy { Math.abs((it.bookingDate?.seconds ?: 0L) - (now.time / 1000)) }
                        .take(5)
                        .map { it.toUiModel(scheduleMap, routeMap, timeFmt, dateFmt) }

                    CombinedData(
                        allSchedules = allScheduleDetails,
                        popularBuses = popularBusesModel,
                        activeRoutes = activeRoutesModel,
                        featuredTrip = upcomingBookings.firstOrNull(),
                        availableOrigins = allScheduleDetails.map { it.origin }.distinct().sorted(),
                        boardingSoon = boardingSoonModel
                    )
                }
                    .flowOn(Dispatchers.Default)
                    .collect { data ->
                        _state.update { current ->
                            val masterDestinations = if (!current.selectedOrigin.isNullOrBlank()) {
                                data.allSchedules
                                    .filter { it.origin.equals(current.selectedOrigin, ignoreCase = true) }
                                    .map { it.destination }.distinct().sorted()
                            } else data.allSchedules.map { it.destination }.distinct().sorted()

                            current.copy(
                                isLoading = false,
                                allSchedules = data.allSchedules,
                                availableSchedules = data.allSchedules,
                                availableOrigins = data.availableOrigins,
                                availableDestinations = masterDestinations,
                                popularBuses = data.popularBuses,
                                activeRoutes = data.activeRoutes,
                                featuredTrip = data.featuredTrip,
                                boardingSoon = data.boardingSoon
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
    fun onLocationTriggerClicked() {
        _state.update { it.copy(isLocating = true) }

        viewModelScope.launch {
            val city = locationService.getCurrentCity()

            if (city != null) {
                _state.update { current ->
                    current.copy(
                        currentLocation = city,
                        selectedOrigin = city, // Syncs with search field
                        isLocating = false
                    )
                }
            } else {
                _state.update { it.copy(isLocating = false) }
                // Handle "City not found" or "Permission missing" UI here
            }
        }
    }

    fun searchBuses() {
        val current = _state.value
        if (current.selectedOrigin.isNullOrBlank() || current.selectedDestination.isNullOrBlank()) {
            _state.update { it.copy(error = "Please select both origin and destination") }
            return
        }

        _state.update { it.copy(isSearching = true) }
        viewModelScope.launch(Dispatchers.Default) {
            val targetDate = current.selectedDateMillis?.let { Date(it) }
            val results = current.allSchedules.filter { sd ->
                val matchesOrigin = sd.origin.equals(current.selectedOrigin, ignoreCase = true)
                val matchesDest = sd.destination.equals(current.selectedDestination, ignoreCase = true)
                val matchesSeats = sd.schedule.availableSeats >= current.passengerCount
                val matchesDate = if (targetDate != null) {
                    sd.schedule.departureDateTime?.toDate()?.let { isSameDay(it, targetDate) } == true
                } else true
                matchesOrigin && matchesDest && matchesSeats && matchesDate
            }
            _state.update { it.copy(isSearching = false, searchResults = results) }
        }
    }

    fun onOriginSelected(city: String?) {
        _state.update { current ->
            val destinationsForNewOrigin = if (!city.isNullOrBlank()) {
                current.allSchedules
                    .filter { it.origin.equals(city, ignoreCase = true) }
                    .map { it.destination }
                    .distinct()
                    .sorted()
            } else current.allSchedules.map { it.destination }.distinct().sorted()

            current.copy(
                selectedOrigin = city,
                selectedDestination = null, // Reset destination for safety
                availableDestinations = destinationsForNewOrigin,
                searchResults = emptyList()
            )
        }
    }

    fun onDestinationSelected(city: String?) {
        _state.update { it.copy(
            selectedDestination = city,
            searchResults = emptyList() // Clear old search results when destination changes
        ) }
    }

    fun onPassengerCountChanged(count: Int) {
        _state.update { it.copy(passengerCount = count) }
    }

    fun onDateSelected(millis: Long?) {
        _state.update {
            it.copy(
                selectedDateMillis = millis,
                formattedDate = Converters.formatDateForUI(millis),
                searchResults = emptyList()
            )
        }
    }
    fun onSwapLocations() {
        _state.update { it.copy(
            selectedOrigin = it.selectedDestination,
            selectedDestination = it.selectedOrigin,
            searchResults = emptyList() // Clear results when locations change
        ) }
    }
    fun updateCurrentLocation(city: String) {
        _state.update { it.copy(
            currentLocation = city,
            selectedOrigin = city // Optionally sync the search origin automatically
        ) }
    }

    private fun buildInitials(first: String, last: String): String =
        "${first.firstOrNull()?.uppercaseChar() ?: ""}${last.firstOrNull()?.uppercaseChar() ?: ""}".ifEmpty { "?" }

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun routeEmoji(origin: String, destination: String): String {
        val combined = "${origin.uppercase()}${destination.uppercase()}"
        return when {
            "LILONGWE" in combined && "BLANTYRE" in combined -> "🏙️"
            "BLANTYRE" in combined && "MZUZU" in combined -> "🌄"
            "LILONGWE" in combined && "MZUZU" in combined -> "🌳"
            "ZOMBA" in combined -> "⛰️"
            "SALIMA" in combined -> "🌊"
            "MANGOCHI" in combined -> "🐟"
            else -> "🚌"
        }
    }

    private fun Booking.toUiModel(
        scheduleMap: Map<String, com.example.booknpay.data.model.Schedule>,
        routeMap: Map<String, Route>,
        timeFmt: SimpleDateFormat,
        dateFmt: SimpleDateFormat
    ): UpcomingBooking {
        val schedule = scheduleMap[this.scheduleId]
        val route = schedule?.let { routeMap[it.routeId] }
        val depDate = schedule?.departureDateTime?.toDate()
        return UpcomingBooking(
            id = this.id,
            bookingReference = this.bookingReference,
            origin = route?.origin ?: "—",
            destination = route?.destination ?: "—",
            departureDate = depDate?.let { dateFmt.format(it) } ?: "—",
            departureTime = depDate?.let { timeFmt.format(it) } ?: "—",
            departureDateMillis = depDate?.time,
            seatNumbers = this.seatNumbers.joinToString(", ").ifEmpty { "—" },
            status = this.bookingStatus,
            paymentStatus = this.paymentStatus,
            totalAmount = this.totalAmount
        )
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.getUserNotificationsRealtime()
                .catch { /* Handle error if needed */ }
                .collect { ns -> _state.update { it.copy(notificationCount = ns.count { !it.isRead }) } }
        }
    }
}

// ─── Internal Helper Data Class ───────────────────────────────────────────────

private data class CombinedData(
    val allSchedules: List<ScheduleDetails>,
    val popularBuses: List<PopularBus>,
    val activeRoutes: List<ActiveRoute>,
    val featuredTrip: UpcomingBooking?,
    val availableOrigins: List<String>,
    val boardingSoon: List<ScheduleDetails>
)