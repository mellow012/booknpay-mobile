package com.example.booknpay.domain.repository

import com.example.booknpay.data.model.ScheduleDetails
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun searchScheduleDetails(
        query: String,
        originCity: String?,
        destinationCity: String?
    ): Flow<List<ScheduleDetails>>

    suspend fun syncSchedules()
}
