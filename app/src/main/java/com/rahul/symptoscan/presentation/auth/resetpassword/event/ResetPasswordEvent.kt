package com.rahul.symptoscan.presentation.auth.resetpassword.event

sealed class ResetPasswordEvent {
    data class PasswordChanged(val password: String) : ResetPasswordEvent()
    data class ConfirmPasswordChanged(val password: String) : ResetPasswordEvent()
    object TogglePasswordVisibility : ResetPasswordEvent()
    object ToggleConfirmPasswordVisibility : ResetPasswordEvent()
    object UpdateClicked : ResetPasswordEvent()
}
