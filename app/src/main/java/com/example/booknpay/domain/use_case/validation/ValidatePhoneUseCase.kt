package com.example.booknpay.domain.use_case.validation

import android.util.Patterns
import javax.inject.Inject

class ValidatePhoneUseCase @Inject constructor() {
    fun execute(phone: String): ValidationResult {
        if (phone.isBlank()) {
            return ValidationResult.Error("Phone number cannot be empty.")
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            return ValidationResult.Error("That's not a valid phone number.")
        }
        return ValidationResult.Success
    }
}
