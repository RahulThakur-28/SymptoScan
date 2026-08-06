package com.rahul.symptoscan.presentation.auth.emailverification.state

data class EmailVerificationState(
    val email: String = "email@example.com",
    val otp: String = "",
    val timer: String = "15:00",
    val isLoading: Boolean = false,
    val isVerified: Boolean = false
) {
    val isVerifyEnabled: Boolean get() = otp.length == 6
}