package com.example.booknpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.booknpay.data.model.Departure
import com.example.booknpay.data.model.DepartureDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartures(departures: List<Departure>): List<Long>

    @Query("""
        SELECT 
            d.*, 
            r.originCity, 
            r.destinationCity 
        FROM departures AS d
        INNER JOIN routes AS r ON d.routeId = r.routeId
        WHERE (:originCity IS NULL OR r.originCity = :originCity)
        AND (:destinationCity IS NULL OR r.destinationCity = :destinationCity)
        AND d.operatorName LIKE '%' || :query || '%'
    """)
    fun searchDepartureDetails(query: String, originCity: String?, destinationCity: String?): Flow<List<DepartureDetails>>

    /**
     * Attempts to reserve a seat for a given departure.
     * This is done by incrementing the `seatsSoldBySafePass` count, but only if there are seats available.
     *
     * @param departureId The ID of the departure.
     * @return The number of rows updated. If 1, the seat was successfully reserved. If 0, no seats were available.
     */
    @Query("""
        UPDATE departures 
        SET seatsSoldBySafePass = seatsSoldBySafePass + 1 
        WHERE departureId = :departureId AND seatsSoldBySafePass < seatsAllocatedToSafePass
    """)
    suspend fun reserveSeat(departureId: Int): Int

    /**
     * Rolls back a seat reservation for a given departure.
     * This is done by decrementing the `seatsSoldBySafePass` count.
     *
     * @param departureId The ID of the departure.
     * @return The number of rows updated.
     */
    @Query("""
        UPDATE departures 
        SET seatsSoldBySafePass = seatsSoldBySafePass - 1 
        WHERE departureId = :departureId AND seatsSoldBySafePass > 0
    """)
    suspend fun rollbackSeatReservation(departureId: Int): Int
}
