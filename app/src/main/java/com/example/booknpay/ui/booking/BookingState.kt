package com.example.booknpay.ui.booking

import com.example.booknpay.data.model.DepartureDetails
import com.example.booknpay.data.model.Ticket

data class BookingState(
    // Data from the database
    val departures: List<DepartureDetails> = emptyList(),
    
    // User selections
    val selectedDeparture: DepartureDetails? = null,
    val searchQuery: String = "",
    val selectedOrigin: String? = null,
    val selectedDestination: String? = null,

    // Form inputs
    val passengerName: String = "",
    val passengerNameError: String? = null,
    val passengerAge: String = "",
    val passengerAgeError: String? = null,
    val passengerSex: String = "",
    val passengerSexError: String? = null,
    val passengerPhone: String = "",
    val passengerPhoneError: String? = null,
    val numberOfPassengers: String = "1",
    val numberOfPassengersError: String? = null,
    val paymentMethod: String = "",
    val paymentMethodError: String? = null,

    // Control flags
    val isLoading: Boolean = false,
    val bookingResult: Ticket? = null,
    val validationPassed: Boolean = false,
    val error: String? = null
)
