package com.example.booknpay.data.model



import com.example.booknpay.data.model.Booking

/**
 * Result wrapper for booking operations
 * More specific than generic AuthResult
 */
sealed class BookingResult {
    data class Success(val booking: Booking) : BookingResult()
    data class Error(val message: String) : BookingResult()
    data object Loading : BookingResult()
    data object SeatsUnavailable : BookingResult() // Specific error for sold out seats
}

/**
 * Legacy support - keeping this for backward compatibility
 * But we should migrate to BookingResult
 */
sealed class BookTicketResult {
    data class Success(val booking: Booking) : BookTicketResult()
    data class Failure(val message: String) : BookTicketResult()
    data object SoldOut : BookTicketResult()
}