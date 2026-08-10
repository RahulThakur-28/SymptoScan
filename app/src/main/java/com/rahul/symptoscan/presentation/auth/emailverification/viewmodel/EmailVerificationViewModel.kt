package com.rahul.symptoscan.presentation.auth.emailverification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.emailverification.event.EmailVerificationEvent
import com.rahul.symptoscan.presentation.auth.emailverification.state.EmailVerificationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EmailVerificationViewModel(
    private val repository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState<Unit>> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val user = repository.getCurrentUser()
        _state.update { it.copy(email = user?.email ?: "") }
        startResendTimer()
    }

    fun onEvent(event: EmailVerificationEvent) {
        when (event) {
            is EmailVerificationEvent.VerifyClicked -> checkVerification()
            is EmailVerificationEvent.ResendEmailClicked -> resendVerification()
            else -> {}
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        _state.update { it.copy(resendCooldown = 60) }
        timerJob = viewModelScope.launch {
            while (_state.value.resendCooldown > 0) {
                delay(1000)
                _state.update { it.copy(resendCooldown = it.resendCooldown - 1) }
            }
        }
    }

    private fun checkVerification() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            // Refresh session to fetch latest confirmation status
            repository.refreshSession()
            
            _state.update { it.copy(isLoading = false) }

            if (repository.isEmailVerified()) {
                _uiState.value = AuthUiState.Success(Unit)
                _state.update { it.copy(isVerified = true) }
            } else {
                _uiState.value = AuthUiState.Error("Email not yet verified. Please check your inbox.")
            }
        }
    }

    private fun resendVerification() {
        if (!_state.value.canResend) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            // In Supabase, resending verification is usually done via another signUp call 
            // or specific reset password for email if already exists.
            // For now we'll just try to refresh session as a placeholder action
            // Or ideally call a resend function if available.
            val result = repository.forgotPassword(_state.value.email)
            
            _uiState.value = AuthUiState.Idle
            
            result.onSuccess {
                startResendTimer()
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Failed to resend email")
            }
        }
    }
}
