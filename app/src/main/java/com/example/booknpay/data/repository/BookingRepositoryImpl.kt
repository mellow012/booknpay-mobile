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

    /**
     * Create booking with Firebase sync
     * Note: routeId is obtained from schedule but NOT stored in booking
     * (to match web app structure)
     */
    override suspend fun createBooking(
        scheduleId: String,
        passengerName: String,
        passengerAge: Int,
        passengerSex: String,
        passengerPhone: String,
        numberOfPassengers: Int
    ): BookTicketResult = withContext(Dispatchers.IO) {
        // Get schedule details for pricing
        val schedule = scheduleDao.getScheduleById(scheduleId)
            ?: return@withContext BookTicketResult.Failure("Schedule not found")

        // Prepare passenger details
        val passengers = listOf(
            Passenger(
                name = passengerName,
                age = passengerAge,
                gender = passengerSex,
                seatNumber = null
            )
        )

        val seatNumbers = emptyList<String>()

        // We pass routeId but Firebase won't store it in booking
        // (it's already in the schedule document)
        return@withContext when (val result = firebaseBookingRepository.createBooking(
            scheduleId = scheduleId,
            passengerDetails = passengers,
            seatNumbers = seatNumbers,
            totalAmount = schedule.price * numberOfPassengers,
            companyId = schedule.companyId,
            routeId = schedule.routeId // Passed but not stored
        )) {
            is BookTicketResult.Success -> {
                bookingDao.insertBooking(result.booking)
                scheduleDao.reserveSeat(scheduleId)
                result
            }
            is BookTicketResult.Failure -> result
            is BookTicketResult.SoldOut -> result
        }
    }

    /**
     * Get all bookings with real-time sync
     */
    override fun getAllBookings(): Flow<List<Booking>> {
        return firebaseBookingRepository.getUserBookingsRealtime()
            .onEach { bookings ->
                bookings.forEach { booking ->
                    bookingDao.insertBooking(booking)
                }
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Search ticket details - combines bookings with schedules and routes
     * Gets routeId from schedule (not from booking)
     */
    override fun searchTicketDetails(query: String): Flow<List<TicketDetails>> {
        return combine(
            bookingDao.getAllBookings(),
            scheduleDao.getAllActiveSchedules(),
            routeDao.getAllRoutes()
        ) { bookings, schedules, routes ->

            val scheduleMap = schedules.associateBy { it.id }
            val routeMap = routes.associateBy { it.id }

            val allTickets = bookings.mapNotNull { booking ->
                // Get schedule to find routeId
                val schedule = scheduleMap[booking.scheduleId] ?: return@mapNotNull null
                // Then get route using routeId from schedule
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

            if (query.isBlank()) {
                allTickets
            } else {
                allTickets.filter { ticketDetail ->
                    ticketDetail.booking.passengerDetails.any { passenger ->
                        passenger.name.contains(query, ignoreCase = true)
                    } || ticketDetail.booking.bookingReference.contains(query, ignoreCase = true)
                            || ticketDetail.originCity.contains(query, ignoreCase = true)
                            || ticketDetail.destinationCity.contains(query, ignoreCase = true)
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Get ticket details for a specific booking reference
     */
    suspend fun getTicketDetailsByReference(bookingReference: String): TicketDetails? {
        return withContext(Dispatchers.IO) {
            val bookings = bookingDao.getAllBookings().map { list ->
                list.find { it.bookingReference == bookingReference }
            }.flowOn(Dispatchers.IO)

            val booking = bookings.first() ?: return@withContext null

            // Get routeId from schedule (not from booking)
            val schedule = scheduleDao.getScheduleById(booking.scheduleId)
                ?: return@withContext null

            val routes = routeDao.getAllRoutes().map { it.associateBy { route -> route.id } }
            val route = routes.first()[schedule.routeId] ?: return@withContext null

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
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }
}