package com.example.booknpay.domain.use_case.validation

import javax.inject.Inject

class ValidatePasswordUseCase @Inject constructor() {
    fun execute(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult.Error("The password needs to consist of at least 8 characters")
        }
        val containsLettersAndDigits = password.any { it.isDigit() } &&
                password.any { it.isLetter() }
        if (!containsLettersAndDigits) {
            return ValidationResult.Error("The password needs to contain at least one letter and one digit")
        }
        return ValidationResult.Success
    }
}
