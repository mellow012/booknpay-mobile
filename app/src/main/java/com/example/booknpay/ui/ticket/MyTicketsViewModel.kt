package com.example.booknpay.ui.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.repository.FirebaseBookingRepository
import com.example.booknpay.util.TicketNotificationHelper
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
    private val bookingRepository: FirebaseBookingRepository,
    private val notificationHelper: TicketNotificationHelper
) : ViewModel() {

    private val _state = MutableStateFlow(MyTicketsState())
    val state: StateFlow<MyTicketsState> = _state.asStateFlow()

    init {
        loadBookings()
    }

    private fun loadBookings() {
        viewModelScope.launch(Dispatchers.IO) {
            bookingRepository.getUserBookingsRealtime()
                .distinctUntilChanged()
                .scan(emptyList<Booking>()) { oldList, newList ->
                    checkForStatusChanges(oldList, newList)
                    newList
                }
                .catch { e -> /* handle error */ }
                .collect { bookings ->
                    _state.update { it.copy(isLoading = false, bookings = bookings) }
                }
        }
    }

    private fun checkForStatusChanges(oldList: List<Booking>, newList: List<Booking>) {
        newList.forEach { newBooking ->
            val oldBooking = oldList.find { it.id == newBooking.id }

            // Logic: Transition from Pending -> Confirmed
            if (oldBooking != null &&
                oldBooking.bookingStatus == "pending" &&
                newBooking.bookingStatus == "confirmed") {

                notificationHelper.showBookingApprovedNotification(newBooking.bookingReference)
            }
        }
    }


    fun cancelBooking(bookingId: String, scheduleId: String, seatNumbers: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = bookingRepository.cancelBooking(bookingId, scheduleId, seatNumbers)
            if (result.isFailure) {
                _state.update { it.copy(error = result.exceptionOrNull()?.message ?: "Failed to cancel") }
            }
        }
    }

    // No onCleared() override — callbackFlow + awaitClose handles Firestore
    // listener teardown automatically when viewModelScope is cancelled.
}