package com.example.booknpay.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.datastore.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NavigationDestination {
    HOME, AUTH, ONBOARDING
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
    private val auth: FirebaseAuth,
    private val preferencesManager: PreferencesManager

) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkFirstLaunchAndAuth()
    }

    private fun checkFirstLaunchAndAuth() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isAnimating = true) }

                // Minimum splash duration for nice animation
                delay(1800)

                val hasSeenOnboarding = preferencesManager.hasSeenOnboarding.first()
                val currentUser = auth.currentUser

                val destination = when {
                    !hasSeenOnboarding -> NavigationDestination.ONBOARDING
                    currentUser != null -> NavigationDestination.HOME
                    else -> NavigationDestination.AUTH
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        navigationDestination = destination
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to initialize app",
                        navigationDestination = NavigationDestination.AUTH
                    )
                }
            }
        }
    }

    // Call this from OnboardingScreen when user finishes
    fun setHasSeenOnboarding() {
        viewModelScope.launch {
            preferencesManager.setHasSeenOnboarding()
        }
    }
}