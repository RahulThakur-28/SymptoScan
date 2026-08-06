package com.rahul.symptoscan.presentation.auth.register.state

data class RegisterState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
) {
    val isRegisterEnabled: Boolean
        get() = fullName.isNotBlank() && email.isNotBlank() && 
                password.isNotBlank() && confirmPassword.isNotBlank() &&
                fullNameError == null && emailError == null && 
                passwordError == null && confirmPasswordError == null
}