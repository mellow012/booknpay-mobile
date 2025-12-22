package com.example.booknpay.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.booknpay.data.model.Ticket
import com.example.booknpay.data.model.TicketDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket): Long

    @Delete
    suspend fun deleteTicket(ticket: Ticket): Int

    @Query("SELECT * FROM tickets WHERE syncStatus = 'PENDING_UPLOAD'")
    suspend fun getPendingSyncTickets(): List<Ticket>

    @Query("UPDATE tickets SET paymentStatus = :paymentStatus, syncStatus = :syncStatus WHERE ticketId = :ticketId")
    suspend fun updateTicketStatus(ticketId: Int, paymentStatus: String, syncStatus: String)

    @Query("SELECT * FROM tickets ORDER BY ticketId DESC")
    fun getAllTickets(): Flow<List<Ticket>>

    @Query("""
        SELECT 
            t.bookingId, 
            t.passengerName, 
            t.paymentStatus, 
            r.originCity AS origin_city, 
            r.destinationCity AS destination_city, 
            d.operatorName AS operator_name
        FROM tickets AS t
        INNER JOIN departures AS d ON t.departureId = d.departureId
        INNER JOIN routes AS r ON d.routeId = r.routeId
        WHERE t.passengerName LIKE '%' || :query || '%'
        ORDER BY t.ticketId DESC
    """)
    fun searchAllTicketDetails(query: String): Flow<List<TicketDetails>>
}
