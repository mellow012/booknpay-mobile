package com.example.booknpay.domain.repository

import com.example.booknpay.data.model.Schedule
import com.example.booknpay.data.model.ScheduleDetails
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    /**
     * Real-time stream of all active schedules combined with route details.
     * Use this in ViewModels — it never completes (infinite Flow).
     */
    fun getScheduleDetailsRealtime(): Flow<List<ScheduleDetails>>

    /**
     * Search/filter schedules with optional origin + destination filters.
     */
    fun searchScheduleDetails(
        query:           String,
        originCity:      String? = null,
        destinationCity: String? = null
    ): Flow<List<ScheduleDetails>>

    /**
     * One-shot fetch of schedules — use for initial cache population only.
     * Returns an empty list on failure (does not throw).
     */
    suspend fun fetchSchedulesOnce(): List<Schedule>

    /**
     * Listen to available seat count for a single schedule.
     * Useful for live seat map updates on the booking screen.
     */
    fun listenToSeatAvailability(scheduleId: String): Flow<Int>

    /**
     * Optimistic local seat reservation (pre-Firebase confirmation).
     * Returns true if a row was updated.
     */
    suspend fun reserveSeatLocally(scheduleId: String): Boolean

    /**
     * Rollback local seat reservation if booking fails.
     */
    suspend fun rollbackSeatReservation(scheduleId: String): Boolean
}

// ─── REMOVED: syncSchedules() ────────────────────────────────────────────────
// The old syncSchedules() called collect() on an infinite Flow inside a
// suspend function, meaning it NEVER returned. The real-time stream from
// getScheduleDetailsRealtime() already keeps the local Room cache in sync
// via the onEach { scheduleDao.insertSchedules(it) } in ScheduleRepositoryImpl.
// No separate sync function is needed.