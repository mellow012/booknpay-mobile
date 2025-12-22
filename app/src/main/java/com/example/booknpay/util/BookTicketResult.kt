package com.example.booknpay.util

import com.example.booknpay.data.model.Ticket

sealed class BookTicketResult {
    data class Success(val ticket: Ticket) : BookTicketResult()
    data class Failure(val errorMessage: String) : BookTicketResult()
    object SoldOut : BookTicketResult()
    object PaymentPending : BookTicketResult() // For USSD
    object Processing : BookTicketResult()
}
