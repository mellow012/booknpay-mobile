package com.example.booknpay.domain.use_case

import com.example.booknpay.data.model.TicketDetails
import com.example.booknpay.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTicketDetailsUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    operator fun invoke(): Flow<List<TicketDetails>> {
        return repository.getAllTicketDetails()
    }
}
