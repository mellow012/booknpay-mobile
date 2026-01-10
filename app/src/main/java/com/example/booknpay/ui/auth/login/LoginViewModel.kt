package com.example.booknpay.ui.auth.login



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.domain.repository.AuthRepository
import com.example.booknpay.domain.use_case.validation.ValidateEmailUseCase
import com.example.booknpay.domain.use_case.validation.ValidatePasswordUseCase
import com.example.booknpay.domain.use_case.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateEmail: ValidateEmailUseCase,
    private val validatePassword: ValidatePasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun showError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        val emailResult = validateEmail.execute(_state.value.email)
        val passwordResult = validatePassword.execute(_state.value.password)

        val hasError = listOf(emailResult, passwordResult).any { it is ValidationResult.Error }

        if (hasError) {
            _state.update {
                it.copy(
                    emailError = (emailResult as? ValidationResult.Error)?.message,
                    passwordError = (passwordResult as? ValidationResult.Error)?.message
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.signInWithEmail(
                _state.value.email,
                _state.value.password
            )) {
                is AuthResult.Success<*> -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true
                        )
                    }
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun onGoogleSignInClick(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success<*> -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true
                        )
                    }
                }
                is AuthResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
