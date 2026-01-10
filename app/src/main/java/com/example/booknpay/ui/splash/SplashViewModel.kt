package com.example.booknpay.ui.splash


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NavigationDestination {
    HOME, AUTH
}

data class SplashState(
    val isLoading: Boolean = true,
    val isAnimating: Boolean = false,
    val error: String? = null,
    val navigationDestination: NavigationDestination? = null,
    val appVersion: String = "1.0.0"
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkAuthAndNavigate()
    }

    private fun checkAuthAndNavigate() {
        viewModelScope.launch {
            try {
                // Start animation
                _state.update { it.copy(isAnimating = true) }

                // Minimum splash duration for smooth UX
                delay(2000)

                // Check authentication status
                val currentUser = auth.currentUser

                // Update navigation destination
                _state.update {
                    it.copy(
                        isLoading = false,
                        navigationDestination = if (currentUser != null) {
                            NavigationDestination.HOME
                        } else {
                            NavigationDestination.AUTH
                        }
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to initialize: ${e.message}",
                        // Navigate to auth on error (safe fallback)
                        navigationDestination = NavigationDestination.AUTH
                    )
                }
            }
        }
    }
}