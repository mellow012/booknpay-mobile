    package com.example.booknpay.ui.booking


    import androidx.lifecycle.SavedStateHandle
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.booknpay.data.repository.FirebaseRouteRepository
    import com.example.booknpay.data.repository.FirebaseScheduleRepository
    import com.example.booknpay.domain.repository.BookingRepository
    import com.example.booknpay.util.BookTicketResult
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.*
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import javax.inject.Inject

    @HiltViewModel
    class BookingViewModel @Inject constructor(
        private val scheduleRepository: FirebaseScheduleRepository,
        private val routeRepository: FirebaseRouteRepository,
        private val bookingRepository: BookingRepository,
        savedStateHandle: SavedStateHandle
    ) : ViewModel() {

        private val scheduleId: String = savedStateHandle.get<String>("scheduleId") ?: ""

        private val _state = MutableStateFlow(BookingState())
        val state: StateFlow<BookingState> = _state.asStateFlow()

        init {
            loadScheduleDetails()
        }

        private fun loadScheduleDetails() {
            viewModelScope.launch(Dispatchers.IO) {
                _state.update { it.copy(isLoading = true) }

                try {
                    combine(
                        scheduleRepository.getSchedulesRealtime(),
                        flow { emit(routeRepository.getRoutes()) }
                    ) { schedules, routes ->
                        val schedule = schedules.find { it.id == scheduleId }
                        val route = schedule?.let { routes.find { r -> r.id == it.routeId } }

                        if (schedule != null && route != null) {
                            com.example.booknpay.data.model.ScheduleDetails(
                                schedule, route.origin, route.destination
                            )
                        } else null
                    }
                        .flowOn(Dispatchers.IO)
                        .catch { e ->
                            withContext(Dispatchers.Main) {
                                _state.update {
                                    it.copy(isLoading = false, error = "Failed to load schedule")
                                }
                            }
                        }
                        .collect { scheduleDetails ->
                            withContext(Dispatchers.Main) {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        scheduleDetails = scheduleDetails
                                    )
                                }
                            }
                        }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _state.update {
                            it.copy(isLoading = false, error = e.message)
                        }
                    }
                }
            }
        }

        fun toggleSeatSelection(seatNumber: String) {
            val current = _state.value.selectedSeats
            val schedule = _state.value.scheduleDetails?.schedule

            // Check if seat is already booked
            val isBooked = schedule?.bookedSeats?.contains(seatNumber) ?: false
            if (isBooked) {
                _state.update { it.copy(error = "Seat $seatNumber is already booked") }
                return
            }

            val updated = if (current.contains(seatNumber)) {
                current - seatNumber
            } else {
                current + seatNumber
            }

            _state.update {
                it.copy(
                    selectedSeats = updated,
                    totalAmount = updated.size * (it.scheduleDetails?.schedule?.price ?: 0.0)
                )
            }
        }

        fun proceedToPassengerDetails() {
            if (_state.value.selectedSeats.isEmpty()) {
                _state.update { it.copy(error = "Please select at least one seat") }
                return
            }

            // Initialize passenger details for each seat
            val passengers = _state.value.selectedSeats.map { seat ->
                PassengerDetail(seatNumber = seat)
            }

            _state.update {
                it.copy(
                    currentStep = BookingStep.PASSENGER_DETAILS,
                    passengerDetails = passengers
                )
            }
        }

        fun updatePassengerDetail(index: Int, detail: PassengerDetail) {
            val updated = _state.value.passengerDetails.toMutableList()
            updated[index] = detail
            _state.update { it.copy(passengerDetails = updated) }
        }

        fun proceedToConfirmation() {
            // Validate passenger details
            val incomplete = _state.value.passengerDetails.any {
                it.name.isBlank() || it.age == 0 || it.gender.isBlank()
            }

            if (incomplete) {
                _state.update { it.copy(error = "Please fill all passenger details") }
                return
            }

            _state.update { it.copy(currentStep = BookingStep.CONFIRMATION) }
        }

        /**
         * This creates a BOOKING REQUEST (not a confirmed booking)
         * Status will be "pending" until operator accepts
         */
        fun submitBookingRequest() {
            viewModelScope.launch(Dispatchers.IO) {
                _state.update { it.copy(isLoading = true) }

                try {
                    val currentState = _state.value
                    val schedule = currentState.scheduleDetails?.schedule
                        ?: throw Exception("Schedule not found")

                    // Convert PassengerDetail to Passenger model
                    val passengers = currentState.passengerDetails.map { detail ->
                        com.example.booknpay.data.model.Passenger(
                            name = detail.name.trim(),
                            age = detail.age,
                            gender = detail.gender,
                            seatNumber = detail.seatNumber
                        )
                    }

                    // Create booking request in Firebase
                    val result = bookingRepository.createBooking(
                        scheduleId = schedule.id,
                        passengerDetails = passengers,
                        seatNumbers = currentState.selectedSeats,
                        totalAmount = currentState.totalAmount,
                        companyId = schedule.companyId,
                        routeId = schedule.routeId
                    )

                    withContext(Dispatchers.Main) {
                        when (result) {
                            is BookTicketResult.Success -> {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        bookingSuccess = true,
                                        bookingReference = result.booking.bookingReference,
                                        error = null
                                    )
                                }
                            }
                            is BookTicketResult.SoldOut -> {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "Selected seats are no longer available. Please select different seats.",
                                        currentStep = BookingStep.SEAT_SELECTION
                                    )
                                }
                            }
                            is BookTicketResult.Failure -> {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        error = result.errorMessage
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to create booking request"
                            )
                        }
                    }
                }
            }
        }

        fun goBack() {
            val previousStep = when (_state.value.currentStep) {
                BookingStep.PASSENGER_DETAILS -> BookingStep.SEAT_SELECTION
                BookingStep.CONFIRMATION -> BookingStep.PASSENGER_DETAILS
                BookingStep.PAYMENT -> BookingStep.CONFIRMATION
                else -> return
            }
            _state.update { it.copy(currentStep = previousStep) }
        }

        fun clearError() {
            _state.update { it.copy(error = null) }
        }

        override fun onCleared() {
            super.onCleared()
            bookingRepository.cleanup()
        }
    }