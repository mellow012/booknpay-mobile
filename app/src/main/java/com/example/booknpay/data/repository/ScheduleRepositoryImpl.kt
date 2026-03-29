package com.example.booknpay.data.repository

import com.example.booknpay.data.local.RouteDao
import com.example.booknpay.data.local.ScheduleDao
import com.example.booknpay.data.model.Schedule
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.domain.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val routeDao: RouteDao,
    private val firebaseScheduleRepository: FirebaseScheduleRepository
) : ScheduleRepository {

    override fun getScheduleDetailsRealtime(): Flow<List<ScheduleDetails>> {
        return combine(
            firebaseScheduleRepository.getSchedulesRealtime(),
            routeDao.getAllRoutes()
        ) { schedules, routes ->
            if (schedules.isNotEmpty()) scheduleDao.insertSchedules(schedules)
            val routeMap = routes.associateBy { it.id }
            schedules.mapNotNull { schedule ->
                routeMap[schedule.routeId]?.let { route ->
                    ScheduleDetails(schedule = schedule, origin = route.origin, destination = route.destination)
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun searchScheduleDetails(
        query: String,
        originCity: String?,
        destinationCity: String?
    ): Flow<List<ScheduleDetails>> {
        return combine(
            scheduleDao.searchSchedules(query, originCity, destinationCity),
            routeDao.getAllRoutes()
        ) { schedules, routes ->
            val routeMap = routes.associateBy { it.id }
            schedules.mapNotNull { schedule ->
                routeMap[schedule.routeId]?.let { route ->
                    ScheduleDetails(schedule = schedule, origin = route.origin, destination = route.destination)
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun fetchSchedulesOnce(): List<Schedule> {
        return try {
            firebaseScheduleRepository.getSchedules()
                .also { schedules ->
                    if (schedules.isNotEmpty()) scheduleDao.insertSchedules(schedules)
                }
        } catch (e: Exception) {
            Timber.e(e, "fetchSchedulesOnce failed")
            emptyList()
        }
    }

    override fun listenToSeatAvailability(scheduleId: String): Flow<Int> {
        return firebaseScheduleRepository.listenToSeatAvailability(scheduleId).flowOn(Dispatchers.IO)
    }

    override suspend fun reserveSeatLocally(scheduleId: String): Boolean {
        return withContext(Dispatchers.IO) { scheduleDao.reserveSeat(scheduleId) > 0 }
    }

    override suspend fun rollbackSeatReservation(scheduleId: String): Boolean {
        return withContext(Dispatchers.IO) { scheduleDao.rollbackSeatReservation(scheduleId) > 0 }
    }

    fun getSchedulesRealtime(): Flow<List<Schedule>> {
        return firebaseScheduleRepository.getSchedulesRealtime()
            .onEach { schedules -> if (schedules.isNotEmpty()) scheduleDao.insertSchedules(schedules) }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Fixed: the old implementation used `return@collect` inside a `collect { }` block,
     * which only exits the lambda — it never assigned the result and always returned null.
     * Now we collect the first emission from the Flow with `.first()` directly.
     */
    suspend fun getScheduleDetailsById(scheduleId: String): ScheduleDetails? {
        return withContext(Dispatchers.IO) {
            val schedule = scheduleDao.getScheduleById(scheduleId) ?: return@withContext null
            val route = routeDao.getAllRoutes().first().find { it.id == schedule.routeId }
                ?: return@withContext null
            ScheduleDetails(
                schedule = schedule,
                origin = route.origin,
                destination = route.destination
            )
        }
    }
}