package com.example.booknpay.domain.repository

import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.model.TicketDetails
import com.example.booknpay.util.BookTicketResult
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    suspend fun createBooking(
        scheduleId: String,
        passengerName: String,
        passengerAge: Int,
        passengerSex: String,
        passengerPhone: String,
        numberOfPassengers: Int
    ): BookTicketResult

    fun getAllBookings(): Flow<List<Booking>>

    fun searchTicketDetails(query: String): Flow<List<TicketDetails>>
}
