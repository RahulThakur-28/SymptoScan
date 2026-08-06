package com.rahul.symptoscan.presentation.auth.emailverification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.presentation.auth.emailverification.event.EmailVerificationEvent
import com.rahul.symptoscan.presentation.auth.emailverification.state.EmailVerificationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmailVerificationViewModel : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    fun onEvent(event: EmailVerificationEvent) {
        when (event) {
            is EmailVerificationEvent.OtpChanged -> {
                _state.update { it.copy(otp = event.otp) }
            }
            is EmailVerificationEvent.VerifyClicked -> {
                verify()
            }
            else -> {}
        }
    }

    private fun verify() {
        if (!_state.value.isVerifyEnabled) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(1500)
            _state.update { it.copy(isLoading = false, isVerified = true) }
        }
    }
}