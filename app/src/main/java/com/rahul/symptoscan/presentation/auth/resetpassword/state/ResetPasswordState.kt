package com.rahul.symptoscan.presentation.auth.resetpassword.state

data class ResetPasswordState(
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false
) {
    val isUpdateEnabled: Boolean
        get() = password.isNotBlank() && confirmPassword.isNotBlank() && 
                passwordError == null && confirmPasswordError == null
}
