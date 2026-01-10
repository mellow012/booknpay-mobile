package com.example.booknpay.ui.booking

import com.example.booknpay.data.model.ScheduleDetails

data class BookingState(
    val isLoading: Boolean = true,
    val scheduleDetails: ScheduleDetails? = null,
    val selectedSeats: List<String> = emptyList(),
    val passengerDetails: List<PassengerDetail> = emptyList(),
    val totalAmount: Double = 0.0,
    val currentStep: BookingStep = BookingStep.SEAT_SELECTION,
    val error: String? = null,
    val bookingSuccess: Boolean = false,
    val bookingReference: String? = null
)

data class PassengerDetail(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val seatNumber: String = ""
)

enum class BookingStep {
    SEAT_SELECTION,
    PASSENGER_DETAILS,
    CONFIRMATION,
    PAYMENT
}