package com.example.booknpay.ui.ticket


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.repository.FirebaseBookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyTicketsState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MyTicketsViewModel @Inject constructor(
    private val bookingRepository: FirebaseBookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyTicketsState())
    val state: StateFlow<MyTicketsState> = _state.asStateFlow()

    init {
        loadBookings()
    }

    private fun loadBookings() {
        viewModelScope.launch(Dispatchers.IO) {
            bookingRepository.getUserBookingsRealtime()
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load bookings"
                        )
                    }
                }
                .collect { bookings ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            bookings = bookings,
                            error = null
                        )
                    }
                }
        }
    }

    fun cancelBooking(bookingId: String, scheduleId: String, seatNumbers: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = bookingRepository.cancelBooking(bookingId, scheduleId, seatNumbers)
            if (result.isFailure) {
                _state.update {
                    it.copy(error = result.exceptionOrNull()?.message ?: "Failed to cancel")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bookingRepository.cleanup()
    }
}