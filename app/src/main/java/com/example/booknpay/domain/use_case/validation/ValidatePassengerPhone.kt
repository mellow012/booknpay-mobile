package com.example.booknpay.domain.use_case.validation

import javax.inject.Inject

class ValidatePassengerPhone @Inject constructor() {
    fun execute(phone: String): ValidationResult {
        if (phone.isBlank()) {
            return ValidationResult.Error("Phone number cannot be empty.")
        }
        // Add more specific phone number validation if needed (e.g., regex)
        return ValidationResult.Success
    }
}
