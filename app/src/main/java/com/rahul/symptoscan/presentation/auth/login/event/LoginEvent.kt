package com.rahul.symptoscan.presentation.auth.login.event

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object LoginClicked : LoginEvent()
    object GoogleLoginClicked : LoginEvent()
    object ForgotPasswordClicked : LoginEvent()
    object RegisterClicked : LoginEvent()
}