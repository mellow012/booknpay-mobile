package com.example.booknpay.util

import com.example.booknpay.data.model.Booking

sealed class BookTicketResult {
    data class Success(val booking: Booking) : BookTicketResult()
    data class Failure(val errorMessage: String) : BookTicketResult()
    object SoldOut : BookTicketResult()
}
