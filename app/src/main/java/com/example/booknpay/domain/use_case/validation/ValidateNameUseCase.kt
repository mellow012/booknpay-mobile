package com.example.booknpay.domain.use_case.validation

import javax.inject.Inject

class ValidateNameUseCase @Inject constructor() {
    fun execute(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("Name cannot be empty.")
        }
        return ValidationResult.Success
    }
}
