package com.rahul.symptoscan.presentation.auth.emailverification.state

data class EmailVerificationState(
    val email: String = "",
    val resendCooldown: Int = 0,
    val isLoading: Boolean = false,
    val isVerified: Boolean = false
) {
    val canResend: Boolean get() = resendCooldown == 0
}
