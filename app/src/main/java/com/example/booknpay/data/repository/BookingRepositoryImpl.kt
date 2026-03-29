package com.example.booknpay.data.repository

import com.example.booknpay.data.local.BookingDao
import com.example.booknpay.data.local.RouteDao
import com.example.booknpay.data.local.ScheduleDao
import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.model.Passenger
import com.example.booknpay.data.model.TicketDetails
import com.example.booknpay.domain.repository.BookingRepository
import com.example.booknpay.util.BookTicketResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val bookingDao: BookingDao,
    private val routeDao: RouteDao,
    private val firebaseBookingRepository: FirebaseBookingRepository
) : BookingRepository {

    // ── Legacy single-passenger overload (kept for backward compat) ───────────
    override suspend fun createBooking(
        scheduleId: String,
        passengerName: String,
        passengerAge: Int,
        passengerSex: String,
        passengerPhone: String,
        numberOfPassengers: Int
    ): BookTicketResult = withContext(Dispatchers.IO) {
        val schedule = scheduleDao.getScheduleById(scheduleId)
            ?: return@withContext BookTicketResult.Failure("Schedule not found")

        val passengers = listOf(
            Passenger(name = passengerName, age = passengerAge, gender = passengerSex, seatNumber = null)
        )
        return@withContext when (val result = firebaseBookingRepository.createBooking(
            scheduleId = scheduleId,
            passengerDetails = passengers,
            seatNumbers = emptyList(),
            totalAmount = schedule.price * numberOfPassengers,
            companyId = schedule.companyId,
            routeId = schedule.routeId
        )) {
            is BookTicketResult.Success -> {
                bookingDao.insertBooking(result.booking)
                scheduleDao.reserveSeat(scheduleId)
                result
            }
            else -> result
        }
    }

    // ── Full multi-passenger overload (used by BookingViewModel) ──────────────
    override suspend fun createBooking(
        scheduleId: String,
        passengerDetails: List<Passenger>,
        seatNumbers: List<String>,
        totalAmount: Double,
        companyId: String,
        routeId: String
    ): BookTicketResult = withContext(Dispatchers.IO) {
        when (val result = firebaseBookingRepository.createBooking(
            scheduleId = scheduleId,
            passengerDetails = passengerDetails,
            seatNumbers = seatNumbers,
            totalAmount = totalAmount,
            companyId = companyId,
            routeId = routeId
        )) {
            is BookTicketResult.Success -> {
                bookingDao.insertBooking(result.booking)
                // Reserve each seat in the local cache
                repeat(seatNumbers.size) { scheduleDao.reserveSeat(scheduleId) }
                result
            }
            else -> result
        }
    }

    // ── Real-time stream — delegates to Firebase, writes through to Room ──────
    override fun getUserBookingsRealtime(): Flow<List<Booking>> {
        return firebaseBookingRepository.getUserBookingsRealtime()
            .onEach { bookings ->
                bookings.forEach { bookingDao.insertBooking(it) }
            }
            .flowOn(Dispatchers.IO)
    }

    // ── Cancel — delegates to Firebase ────────────────────────────────────────
    override suspend fun cancelBooking(
        bookingId: String,
        scheduleId: String,
        seatNumbers: List<String>
    ): Result<Unit> = firebaseBookingRepository.cancelBooking(bookingId, scheduleId, seatNumbers)

    // ── All bookings (local cache) ────────────────────────────────────────────
    override fun getAllBookings(): Flow<List<Booking>> {
        return firebaseBookingRepository.getUserBookingsRealtime()
            .onEach { bookings -> bookings.forEach { bookingDao.insertBooking(it) } }
            .flowOn(Dispatchers.IO)
    }

    // ── Search ticket details ─────────────────────────────────────────────────
    override fun searchTicketDetails(query: String): Flow<List<TicketDetails>> {
        return combine(
            bookingDao.getAllBookings(),
            scheduleDao.getAllActiveSchedules(),
            routeDao.getAllRoutes()
        ) { bookings, schedules, routes ->
            val scheduleMap = schedules.associateBy { it.id }
            val routeMap = routes.associateBy { it.id }

            val allTickets = bookings.mapNotNull { booking ->
                val schedule = scheduleMap[booking.scheduleId] ?: return@mapNotNull null
                val route = routeMap[schedule.routeId] ?: return@mapNotNull null
                TicketDetails(
                    booking = booking,
                    originCity = route.origin,
                    destinationCity = route.destination,
                    operatorName = schedule.busId,
                    departureTime = formatTime(schedule.departureDateTime?.toDate()),
                    arrivalTime = formatTime(schedule.arrivalDateTime?.toDate())
                )
            }

            if (query.isBlank()) allTickets
            else allTickets.filter { td ->
                td.booking.passengerDetails.any { it.name.contains(query, ignoreCase = true) }
                        || td.booking.bookingReference.contains(query, ignoreCase = true)
                        || td.originCity.contains(query, ignoreCase = true)
                        || td.destinationCity.contains(query, ignoreCase = true)
            }
        }.flowOn(Dispatchers.IO)
    }

    // ── cleanup() is a no-op — callbackFlow handles listener lifecycle ────────
    override fun cleanup(){
        firebaseBookingRepository.cleanup()
    }

    // ── Convenience: look up a single ticket by booking reference ─────────────
    suspend fun getTicketDetailsByReference(bookingReference: String): TicketDetails? {
        return withContext(Dispatchers.IO) {
            val booking = bookingDao.getAllBookings()
                .map { list -> list.find { it.bookingReference == bookingReference } }
                .first() ?: return@withContext null

            val schedule = scheduleDao.getScheduleById(booking.scheduleId)
                ?: return@withContext null

            val route = routeDao.getAllRoutes().first().find { it.id == schedule.routeId }
                ?: return@withContext null

            TicketDetails(
                booking = booking,
                originCity = route.origin,
                destinationCity = route.destination,
                operatorName = schedule.busId,
                departureTime = formatTime(schedule.departureDateTime?.toDate()),
                arrivalTime = formatTime(schedule.arrivalDateTime?.toDate())
            )
        }
    }

    private fun formatTime(date: Date?): String {
        if (date == null) return "N/A"
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}