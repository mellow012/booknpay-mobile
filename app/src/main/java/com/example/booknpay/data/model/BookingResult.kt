package com.example.booknpay.data.model

/**
 * Result wrapper for booking operations.
 * BookTicketResult lives in com.example.booknpay.util — use that everywhere.
 * This file only keeps the richer BookingResult used by the admin/operator side.
 */
sealed class BookingResult {
    data class Success(val booking: Booking) : BookingResult()
    data class Error(val message: String) : BookingResult()
    data object Loading : BookingResult()
    data object SeatsUnavailable : BookingResult()
}