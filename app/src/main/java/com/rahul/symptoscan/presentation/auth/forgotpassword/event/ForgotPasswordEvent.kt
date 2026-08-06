package com.rahul.symptoscan.presentation.auth.forgotpassword.event

sealed class ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent()
    object SubmitClicked : ForgotPasswordEvent()
}
