package com.example.booknpay.domain.use_case

import com.example.booknpay.data.model.DepartureDetails
import com.example.booknpay.domain.repository.DepartureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchDeparturesUseCase @Inject constructor(
    private val repository: DepartureRepository
) {
    operator fun invoke(
        query: String,
        originCity: String?,
        destinationCity: String?
    ): Flow<List<DepartureDetails>> {
        return repository.searchDepartureDetails(query, originCity, destinationCity)
    }
}
