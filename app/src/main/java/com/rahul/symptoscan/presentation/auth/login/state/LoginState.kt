package com.rahul.symptoscan.presentation.auth.login.state

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
) {
    val isSignInEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null
}