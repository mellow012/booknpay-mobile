package com.example.booknpay.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.data.repository.ThemeRepository
import com.example.booknpay.data.repository.UserRepository
import com.example.booknpay.domain.repository.AuthRepository
import com.example.booknpay.domain.repository.BookingRepository
import com.example.booknpay.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository,
    private  val notificationRepository: NotificationRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val state: StateFlow<ProfileState> = combine(
        userRepository.getCurrentUser(),
        bookingRepository.getUserBookingsRealtime(),
        themeRepository.isDarkMode
    ) { user, bookings, isDark ->
        ProfileState(
            user = user,
            totalTrips = bookings.size,
            upcomingTrips = bookings.count { it.bookingStatus == "pending" || it.bookingStatus == "confirmed" },
            isDarkMode = isDark,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileState()
    )

    fun toggleTheme(enabled: Boolean) {
        viewModelScope.launch { themeRepository.toggleTheme(enabled) }
    }
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            notificationRepository.cleanup()
            bookingRepository.cleanup()
            val result = authRepository.signOut()
            if (result is AuthResult.Success) {
                onComplete()
            } else {
                // Handle error if sign out fails (rare, but good for logs)
            }
        }
    }


    fun updateProfile(firstName: String, lastName: String, nationalId: String, address: String) {
        val currentUser = state.value.user ?: return
        val updatedUser = currentUser.copy(
            firstName = firstName,
            lastName = lastName,
            nationalId = nationalId,
            currentAddress = address,
            setupCompleted = true
        )

        viewModelScope.launch {
            userRepository.updateUser(updatedUser)
        }
    }
}
