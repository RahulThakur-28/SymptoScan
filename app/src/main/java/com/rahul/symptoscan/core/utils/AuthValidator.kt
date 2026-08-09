package com.rahul.symptoscan.core.utils

object AuthValidator {
    
    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): PasswordValidationResult {
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return PasswordValidationResult(
            hasMinLength = hasMinLength,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasDigit = hasDigit,
            hasSpecialChar = hasSpecialChar
        )
    }

    data class PasswordValidationResult(
        val hasMinLength: Boolean,
        val hasUppercase: Boolean,
        val hasLowercase: Boolean,
        val hasDigit: Boolean,
        val hasSpecialChar: Boolean
    ) {
        val isValid: Boolean
            get() = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    }
}
