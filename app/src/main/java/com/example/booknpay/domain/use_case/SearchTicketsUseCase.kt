package com.example.booknpay.domain.use_case

import com.example.booknpay.data.model.TicketDetails
import com.example.booknpay.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchTicketsUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    operator fun invoke(query: String): Flow<List<TicketDetails>> {
        return repository.searchAllTicketDetails(query)
    }
}
