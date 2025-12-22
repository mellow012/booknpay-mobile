package com.example.booknpay.domain.repository

import com.example.booknpay.data.model.DepartureDetails
import kotlinx.coroutines.flow.Flow

interface DepartureRepository {
    fun searchDepartureDetails(
        query: String,
        originCity: String?,
        destinationCity: String?
    ): Flow<List<DepartureDetails>>
}
