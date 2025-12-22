package com.example.booknpay.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.DepartureDetails
import com.example.booknpay.domain.use_case.BookTicketUseCase
import com.example.booknpay.domain.use_case.SearchDeparturesUseCase
import com.example.booknpay.domain.use_case.validation.ValidatePassengerAge
import com.example.booknpay.domain.use_case.validation.ValidatePassengerName
import com.example.booknpay.domain.use_case.validation.ValidatePassengerPhone
import com.example.booknpay.domain.use_case.validation.ValidationResult
import com.example.booknpay.util.BookTicketResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val searchDeparturesUseCase: SearchDeparturesUseCase,
    private val bookTicketUseCase: BookTicketUseCase,
    private val validatePassengerName: ValidatePassengerName,
    private val validatePassengerAge: ValidatePassengerAge,
    private val validatePassengerPhone: ValidatePassengerPhone
) : ViewModel() {

    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state.asStateFlow()

    init {
        // Initial load of all departures
        onEvent(BookingEvent.SearchDepartures("", null, null))
    }

    fun onEvent(event: BookingEvent) {
        when (event) {
            is BookingEvent.SearchDepartures -> {
                _state.update { 
                    it.copy(
                        searchQuery = event.query,
                        selectedOrigin = event.origin,
                        selectedDestination = event.destination
                    )
                }
                searchDeparturesUseCase(event.query, event.origin, event.destination)
                    .onEach { departures ->
                        _state.update { it.copy(departures = departures) }
                    }
                    .launchIn(viewModelScope)
            }
            is BookingEvent.SelectDeparture -> {
                _state.update { it.copy(selectedDeparture = event.departure) }
            }
            is BookingEvent.PassengerNameChanged -> {
                _state.update { it.copy(passengerName = event.name) }
            }
            is BookingEvent.PassengerAgeChanged -> {
                _state.update { it.copy(passengerAge = event.age) }
            }
            is BookingEvent.PassengerSexChanged -> {
                _state.update { it.copy(passengerSex = event.sex) }
            }
            is BookingEvent.PassengerPhoneChanged -> {
                _state.update { it.copy(passengerPhone = event.phone) }
            }
            is BookingEvent.NumberOfPassengersChanged -> {
                _state.update { it.copy(numberOfPassengers = event.count) }
            }
            is BookingEvent.PaymentMethodChanged -> {
                _state.update { it.copy(paymentMethod = event.method) }
            }
            is BookingEvent.ValidateForm -> validateForm()
            is BookingEvent.BookTicket -> bookTicket()
            is BookingEvent.BookingFlowCompleted -> {
                _state.update { it.copy(bookingResult = null, validationPassed = false) } // Reset flags
            }
        }
    }

    private fun validateForm() {
        val state = _state.value
        val nameResult = validatePassengerName.execute(state.passengerName)
        val ageResult = validatePassengerAge.execute(state.passengerAge)
        val phoneResult = validatePassengerPhone.execute(state.passengerPhone)

        val hasError = listOf(nameResult, ageResult, phoneResult).any { it is ValidationResult.Error }

        if (hasError) {
            _state.update {
                it.copy(
                    passengerNameError = (nameResult as? ValidationResult.Error)?.message,
                    passengerAgeError = (ageResult as? ValidationResult.Error)?.message,
                    passengerPhoneError = (phoneResult as? ValidationResult.Error)?.message,
                    validationPassed = false
                )
            }
        } else {
             _state.update {
                it.copy(
                    passengerNameError = null,
                    passengerAgeError = null,
                    passengerPhoneError = null,
                    validationPassed = true
                )
            }
        }
    }

    private fun bookTicket() {
        // The form is now validated before navigating here, so we just book.
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, bookingResult = null, error = null) }

            val currentState = _state.value
            val selectedDeparture = currentState.selectedDeparture!! // Should not be null here

            val result = bookTicketUseCase(
                departureId = selectedDeparture.departure.departureId,
                passengerName = currentState.passengerName,
                passengerAge = currentState.passengerAge.toInt(),
                passengerSex = currentState.passengerSex,
                passengerPhone = currentState.passengerPhone,
                numberOfPassengers = currentState.numberOfPassengers.toInt()
            )

            _state.update { 
                when (result) {
                    is BookTicketResult.Success -> it.copy(isLoading = false, bookingResult = result.ticket)
                    is BookTicketResult.Failure -> it.copy(isLoading = false, error = result.errorMessage)
                    is BookTicketResult.SoldOut -> it.copy(isLoading = false, error = "Sorry, this departure is sold out.")
                    else -> it.copy(isLoading = false, error = "An unexpected error occurred.")
                }
            }
        }
    }
}

sealed class BookingEvent {
    data class SearchDepartures(val query: String, val origin: String?, val destination: String?) : BookingEvent()
    data class SelectDeparture(val departure: DepartureDetails) : BookingEvent()
    data class PassengerNameChanged(val name: String) : BookingEvent()
    data class PassengerAgeChanged(val age: String) : BookingEvent()
    data class PassengerSexChanged(val sex: String) : BookingEvent()
    data class PassengerPhoneChanged(val phone: String) : BookingEvent()
    data class NumberOfPassengersChanged(val count: String) : BookingEvent()
    data class PaymentMethodChanged(val method: String) : BookingEvent()
    object ValidateForm : BookingEvent()
    object BookTicket : BookingEvent()
    object BookingFlowCompleted : BookingEvent()
}
