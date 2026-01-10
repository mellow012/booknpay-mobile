package com.example.booknpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.booknpay.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<Schedule>)

    // Get schedules with filters - returns just Schedule, combine with Route in repository
    @Query("""
        SELECT s.* 
        FROM schedules AS s
        INNER JOIN routes AS r ON s.routeId = r.id
        WHERE (:origin IS NULL OR r.origin = :origin)
        AND (:destination IS NULL OR r.destination = :destination)
        AND s.busId LIKE '%' || :query || '%'
        ORDER BY s.departureDateTime ASC
    """)
    fun searchSchedules(query: String, origin: String?, destination: String?): Flow<List<Schedule>>

    @Query("UPDATE schedules SET availableSeats = availableSeats - 1 WHERE id = :scheduleId AND availableSeats > 0")
    suspend fun reserveSeat(scheduleId: String): Int

    @Query("UPDATE schedules SET availableSeats = availableSeats + 1 WHERE id = :scheduleId")
    suspend fun rollbackSeatReservation(scheduleId: String): Int

    @Query("SELECT * FROM schedules WHERE id = :scheduleId LIMIT 1")
    suspend fun getScheduleById(scheduleId: String): Schedule?

    @Query("SELECT * FROM schedules WHERE isActive = 1 AND status = 'active' ORDER BY departureDateTime ASC")
    fun getAllActiveSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules")
    suspend fun getAllSchedulesList(): List<Schedule>

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
}