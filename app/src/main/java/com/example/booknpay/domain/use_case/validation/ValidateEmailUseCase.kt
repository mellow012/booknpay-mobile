package com.example.booknpay.domain.use_case.validation

import android.util.Patterns
import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor() {
    fun execute(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult.Error("Email cannot be empty.")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult.Error("That's not a valid email.")
        }
        return ValidationResult.Success
    }
}
