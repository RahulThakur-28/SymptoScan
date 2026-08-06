package com.rahul.symptoscan.presentation.auth.forgotpassword.state

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false
) {
    val isSubmitEnabled: Boolean get() = email.isNotBlank() && emailError == null
}
