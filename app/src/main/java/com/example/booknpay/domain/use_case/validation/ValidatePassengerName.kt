package com.example.booknpay.domain.use_case.validation

import javax.inject.Inject

class ValidatePassengerName @Inject constructor() {
    fun execute(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("Passenger name cannot be empty.")
        }
        return ValidationResult.Success
    }
}
