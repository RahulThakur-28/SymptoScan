package com.rahul.symptoscan.presentation.auth.register.event

sealed class RegisterEvent {
    data class FullNameChanged(val name: String) : RegisterEvent()
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val password: String) : RegisterEvent()
    object TogglePasswordVisibility : RegisterEvent()
    object ToggleConfirmPasswordVisibility : RegisterEvent()
    object RegisterClicked : RegisterEvent()
    object LoginClicked : RegisterEvent()
}