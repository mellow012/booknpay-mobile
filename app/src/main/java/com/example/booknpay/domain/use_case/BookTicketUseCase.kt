package com.example.booknpay.domain.use_case

import com.example.booknpay.domain.repository.TicketRepository
import com.example.booknpay.util.BookTicketResult
import javax.inject.Inject

class BookTicketUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(
        departureId: Int,
        passengerName: String,
        passengerAge: Int,
        passengerSex: String,
        passengerPhone: String,
        numberOfPassengers: Int
    ): BookTicketResult {
        return repository.bookTicket(
            departureId = departureId,
            passengerName = passengerName,
            passengerAge = passengerAge,
            passengerSex = passengerSex,
            passengerPhone = passengerPhone,
            numberOfPassengers = numberOfPassengers
        )
    }
}
