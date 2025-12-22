package com.example.booknpay.data.repository

import com.example.booknpay.data.local.DepartureDao
import com.example.booknpay.data.local.TicketDao
import com.example.booknpay.data.model.Ticket
import com.example.booknpay.data.model.TicketDetails
import com.example.booknpay.domain.repository.TicketRepository
import com.example.booknpay.util.BookTicketResult
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val departureDao: DepartureDao,
    private val ticketDao: TicketDao
) : TicketRepository {

    override suspend fun bookTicket(
        departureId: Int,
        passengerName: String,
        passengerAge: Int,
        passengerSex: String,
        passengerPhone: String,
        numberOfPassengers: Int
    ): BookTicketResult {
        // Step 1 (Offline): Try to reserve a seat in the local database.
        val seatReserved = departureDao.reserveSeat(departureId) > 0

        if (!seatReserved) {
            return BookTicketResult.SoldOut
        }

        // Create a temporary ticket with a PENDING status.
        val bookingId = UUID.randomUUID().toString()
        val pendingTicket = Ticket(
            bookingId = bookingId,
            departureId = departureId,
            passengerName = passengerName,
            passengerAge = passengerAge,
            passengerSex = passengerSex,
            passengerPhone = passengerPhone,
            numberOfPassengers = numberOfPassengers,
            paymentStatus = "PENDING",
            syncStatus = "PENDING_UPLOAD"
        )
        val ticketId = ticketDao.insertTicket(pendingTicket)

        // Step 2 (Network): Simulate payment (API call or USSD).
        // In a real app, this would be a network call to your server.
        val isPaymentSuccessful = simulatePaymentApiCall()

        return if (isPaymentSuccessful) {
            // Step 3 (Success): Update ticket status to CONFIRMED.
            val confirmedTicket = pendingTicket.copy(
                ticketId = ticketId.toInt(),
                paymentStatus = "CONFIRMED"
            )
            ticketDao.updateTicketStatus(
                ticketId = ticketId.toInt(),
                paymentStatus = "CONFIRMED",
                syncStatus = "PENDING_UPLOAD" // It still needs to be synced with the server
            )
            BookTicketResult.Success(confirmedTicket)
        } else {
            // Step 3 (Failure): Rollback the transaction.
            departureDao.rollbackSeatReservation(departureId)
            // We need to pass the full ticket object to delete it
            val ticketToDelete = pendingTicket.copy(ticketId = ticketId.toInt())
            ticketDao.deleteTicket(ticketToDelete)
            BookTicketResult.Failure("Payment failed. Please try again.")
        }
    }

    override fun getAllTickets(): Flow<List<Ticket>> {
        return ticketDao.getAllTickets()
    }

    override fun getAllTicketDetails(): Flow<List<TicketDetails>> {
        return ticketDao.searchAllTicketDetails("")
    }

    override fun searchAllTicketDetails(query: String): Flow<List<TicketDetails>> {
        return ticketDao.searchAllTicketDetails(query)
    }

    private fun simulatePaymentApiCall(): Boolean {
        // Simulate a network delay and a 70% success rate.
        Thread.sleep(1500)
        return Math.random() < 0.7
    }
}
