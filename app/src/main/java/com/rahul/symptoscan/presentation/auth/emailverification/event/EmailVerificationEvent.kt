package com.rahul.symptoscan.presentation.auth.emailverification.event

sealed class EmailVerificationEvent {
    data class OtpChanged(val otp: String) : EmailVerificationEvent()
    object VerifyClicked : EmailVerificationEvent()
    object ResendEmailClicked : EmailVerificationEvent()
    object BackClicked : EmailVerificationEvent()
}