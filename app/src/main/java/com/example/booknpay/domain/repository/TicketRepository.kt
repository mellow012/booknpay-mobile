package com.example.booknpay.domain.repository

import com.example.booknpay.data.model.Ticket
import com.example.booknpay.data.model.TicketDetails
import com.example.booknpay.util.BookTicketResult
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    suspend fun bookTicket(
        departureId: Int,
        passengerName: String,
        passengerAge: Int,
        passengerSex: String,
        passengerPhone: String,
        numberOfPassengers: Int
    ): BookTicketResult

    fun getAllTickets(): Flow<List<Ticket>>

    fun getAllTicketDetails(): Flow<List<TicketDetails>>

    fun searchAllTicketDetails(query: String): Flow<List<TicketDetails>>
}
