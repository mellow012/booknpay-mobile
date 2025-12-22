package com.example.booknpay.domain.use_case

import com.example.booknpay.data.model.Ticket
import com.example.booknpay.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTicketsUseCase @Inject constructor(
    private val repository: TicketRepository
) {
    operator fun invoke(): Flow<List<Ticket>> {
        return repository.getAllTickets()
    }
}
