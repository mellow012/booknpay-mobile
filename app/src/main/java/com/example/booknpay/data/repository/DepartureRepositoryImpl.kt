package com.example.booknpay.data.repository

import com.example.booknpay.data.local.DepartureDao
import com.example.booknpay.data.model.DepartureDetails
import com.example.booknpay.domain.repository.DepartureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DepartureRepositoryImpl @Inject constructor(
    private val departureDao: DepartureDao
) : DepartureRepository {

    override fun searchDepartureDetails(
        query: String,
        originCity: String?,
        destinationCity: String?
    ): Flow<List<DepartureDetails>> {
        return departureDao.searchDepartureDetails(query, originCity, destinationCity)
    }
}
