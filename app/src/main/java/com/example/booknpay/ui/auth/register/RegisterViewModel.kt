package com.example.booknpay.ui.auth.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.data.model.RegisterRequest
import com.example.booknpay.domain.repository.AuthRepository
import com.example.booknpay.domain.use_case.validation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateEmail: ValidateEmailUseCase,
    private val validatePassword: ValidatePasswordUseCase,
    private val validateName: ValidateNameUseCase,
    private val validatePhone: ValidatePhoneUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onFirstNameChange(firstName: String) {
        _state.update { it.copy(firstName = firstName, firstNameError = null) }
    }

    fun onLastNameChange(lastName: String) {
        _state.update { it.copy(lastName = lastName, lastNameError = null) }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onPhoneChange(phone: String) {
        _state.update { it.copy(phone = phone, phoneError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onRegisterClick() {
        val firstNameResult = validateName.execute(_state.value.firstName)
        val lastNameResult = validateName.execute(_state.value.lastName)
        val emailResult = validateEmail.execute(_state.value.email)
        val phoneResult = validatePhone.execute(_state.value.phone)
        val passwordResult = validatePassword.execute(_state.value.password)

        val confirmPasswordResult = if (_state.value.password != _state.value.confirmPassword) {
            ValidationResult.Error("Passwords do not match")
        } else {
            ValidationResult.Success
        }

        val hasError = listOf(
            firstNameResult,
            lastNameResult,
            emailResult,
            phoneResult,
            passwordResult,
            confirmPasswordResult
        ).any { it is ValidationResult.Error }

        if (hasError) {
            _state.update {
                it.copy(
                    firstNameError = (firstNameResult as? ValidationResult.Error)?.message,
                    lastNameError = (lastNameResult as? ValidationResult.Error)?.message,
                    emailError = (emailResult as? ValidationResult.Error)?.message,
                    phoneError = (phoneResult as? ValidationResult.Error)?.message,
                    passwordError = (passwordResult as? ValidationResult.Error)?.message,
                    confirmPasswordError = (confirmPasswordResult as? ValidationResult.Error)?.message
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val request = RegisterRequest(
                email = _state.value.email,
                password = _state.value.password,
                firstName = _state.value.firstName,
                lastName = _state.value.lastName,
                phone = _state.value.phone
            )

            when (val result = authRepository.signUpWithEmail(request)) {
                is AuthResult.Success<*> -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRegistrationSuccessful = true
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
