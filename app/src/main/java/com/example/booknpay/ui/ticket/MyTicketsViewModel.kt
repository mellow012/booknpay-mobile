package com.example.booknpay.ui.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.TicketDetails
import com.example.booknpay.domain.use_case.SearchTicketsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MyTicketsViewModel @Inject constructor(
    private val searchTicketsUseCase: SearchTicketsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyTicketsState())
    val state: StateFlow<MyTicketsState> = _state.asStateFlow()

    init {
        onEvent(MyTicketsEvent.SearchTickets(""))
    }

    fun onEvent(event: MyTicketsEvent) {
        when (event) {
            is MyTicketsEvent.SearchTickets -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                searchTicketsUseCase(event.query)
                    .onEach { tickets ->
                        _state.value = _state.value.copy(tickets = tickets)
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
}

data class MyTicketsState(
    val tickets: List<TicketDetails> = emptyList(),
    val searchQuery: String = ""
)

sealed class MyTicketsEvent {
    data class SearchTickets(val query: String) : MyTicketsEvent()
}
