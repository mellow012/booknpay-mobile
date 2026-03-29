package com.example.booknpay.domain.repository

import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.model.Passenger
import com.example.booknpay.data.model.TicketDetails
import com.example.booknpay.util.BookTicketResult
import kotlinx.coroutines.flow.Flow

interface BookingRepository {

    /**
     * Create a booking with full passenger + seat details.
     */
    suspend fun createBooking(
        scheduleId:       String,
        passengerDetails: List<Passenger>,
        seatNumbers:      List<String>,
        totalAmount:      Double,
        companyId:        String,
        routeId:          String
    ): BookTicketResult

    /**
     * Legacy/Simple booking for single passengers.
     */
    suspend fun createBooking(
        scheduleId: String,
        passengerName: String,
        passengerAge: Int,
        passengerSex: String,
        passengerPhone: String,
        numberOfPassengers: Int
    ): BookTicketResult

    fun getUserBookingsRealtime(): Flow<List<Booking>>

    fun getAllBookings(): Flow<List<Booking>>

    suspend fun cancelBooking(
        bookingId:   String,
        scheduleId:  String,
        seatNumbers: List<String>
    ): Result<Unit>

    fun searchTicketDetails(query: String): Flow<List<TicketDetails>>

    /**
     * Clean up any active Firestore listeners.
     * Implementation can be Unit if using callbackFlow.
     */
    fun cleanup()
} // Ensure there is only one closing brace here