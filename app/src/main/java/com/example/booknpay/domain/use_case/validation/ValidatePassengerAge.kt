package com.example.booknpay.domain.use_case.validation

import javax.inject.Inject

class ValidatePassengerAge @Inject constructor() {
    fun execute(age: String): ValidationResult {
        if (age.isBlank()) {
            return ValidationResult.Error("Age cannot be empty.")
        }
        if (age.toIntOrNull() == null || age.toInt() <= 0) {
            return ValidationResult.Error("Please enter a valid age.")
        }
        return ValidationResult.Success
    }
}
