package com.example.booknpay.data.repository

import com.example.booknpay.data.local.RouteDao
import com.example.booknpay.data.local.ScheduleDao
import com.example.booknpay.data.model.Schedule
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.domain.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val routeDao: RouteDao,
    private val firebaseScheduleRepository: FirebaseScheduleRepository
) : ScheduleRepository {

    /**
     * Search schedules with real-time updates from Firebase
     * Combines schedules with routes to create ScheduleDetails
     */
    override fun searchScheduleDetails(
        query: String,
        originCity: String?,
        destinationCity: String?
    ): Flow<List<ScheduleDetails>> {
        // Combine schedules from DAO with routes to build ScheduleDetails
        return combine(
            scheduleDao.searchSchedules(query, originCity, destinationCity),
            routeDao.getAllRoutes()
        ) { schedules, routes ->
            val routeMap = routes.associateBy { it.id }

            schedules.mapNotNull { schedule ->
                routeMap[schedule.routeId]?.let { route ->
                    ScheduleDetails(
                        schedule = schedule,
                        origin = route.origin,
                        destination = route.destination
                    )
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Get all schedule details (schedules combined with routes)
     */
    /**
     * Sync schedules from Firebase with real-time updates
     * This keeps local cache in sync with Firestore
     */
    override suspend fun syncSchedules() = withContext(Dispatchers.IO) {
        firebaseScheduleRepository.getSchedulesRealtime()
            .catch { e ->
                // Log error but don't crash
                println("Error syncing schedules: ${e.message}")
            }
            .collect { schedules ->
                // Update local cache
                if (schedules.isNotEmpty()) {
                    // Clear old data and insert new
                    scheduleDao.deleteAll()
                    scheduleDao.insertSchedules(schedules)
                }
            }
    }

    /**
     * Get schedules with real-time updates (for UI)
     * Returns raw Schedule objects, not ScheduleDetails
     */
    fun getSchedulesRealtime(): Flow<List<Schedule>> {
        return firebaseScheduleRepository.getSchedulesRealtime()
            .onEach { schedules ->
                // Update local cache in background
                if (schedules.isNotEmpty()) {
                    scheduleDao.insertSchedules(schedules)
                }
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get schedules with route details in real-time
     * This is what you should use in ViewModels
     */
    fun getScheduleDetailsRealtime(): Flow<List<ScheduleDetails>> {
        return combine(
            firebaseScheduleRepository.getSchedulesRealtime(),
            routeDao.getAllRoutes()
        ) { schedules, routes ->
            // Update local cache
            if (schedules.isNotEmpty()) {
                scheduleDao.insertSchedules(schedules)
            }

            // Build ScheduleDetails
            val routeMap = routes.associateBy { it.id }
            schedules.mapNotNull { schedule ->
                routeMap[schedule.routeId]?.let { route ->
                    ScheduleDetails(
                        schedule = schedule,
                        origin = route.origin,
                        destination = route.destination
                    )
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Get a single schedule by ID with route details
     */
    suspend fun getScheduleDetailsById(scheduleId: String): ScheduleDetails? {
        return withContext(Dispatchers.IO) {
            val schedule = scheduleDao.getScheduleById(scheduleId) ?: return@withContext null
            val routes = routeDao.getAllRoutes()
            val routeMap = routes.map { it.associateBy { route -> route.id } }.first()
            val route = routeMap[schedule.routeId] ?: return@withContext null

            ScheduleDetails(
                schedule = schedule,
                origin = route.origin,
                destination = route.destination
            )
        }
    }

    /**
     * Listen to seat availability for a specific schedule
     * Useful for booking screen to show real-time updates
     */
    fun listenToSeatAvailability(scheduleId: String): Flow<Int> {
        return firebaseScheduleRepository.listenToSeatAvailability(scheduleId)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Reserve a seat locally (for optimistic updates)
     */
    suspend fun reserveSeat(scheduleId: String): Boolean {
        return withContext(Dispatchers.IO) {
            scheduleDao.reserveSeat(scheduleId) > 0
        }
    }

    /**
     * Rollback seat reservation if booking fails
     */
    suspend fun rollbackSeatReservation(scheduleId: String): Boolean {
        return withContext(Dispatchers.IO) {
            scheduleDao.rollbackSeatReservation(scheduleId) > 0
        }
    }
}