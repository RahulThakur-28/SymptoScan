package com.rahul.symptoscan.presentation.auth.emailverification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.emailverification.event.EmailVerificationEvent
import com.rahul.symptoscan.presentation.auth.emailverification.state.EmailVerificationState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EmailVerificationViewModel(
    private val repository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState<Unit>> = _uiState.asStateFlow()

    fun onEvent(event: EmailVerificationEvent) {
        when (event) {
            is EmailVerificationEvent.VerifyClicked -> checkVerification()
            is EmailVerificationEvent.ResendEmailClicked -> resendVerification()
            else -> {}
        }
    }

    private fun checkVerification() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            // Refresh session to fetch latest confirmation status
            repository.refreshSession()
            
            if (repository.isEmailVerified()) {
                _uiState.value = AuthUiState.Success(Unit)
                _state.update { it.copy(isLoading = false, isVerified = true) }
            } else {
                _uiState.value = AuthUiState.Error("Email not yet verified. Please check your inbox.")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun resendVerification() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            // For resending verification, we can trigger a sign-in or a custom flow if Supabase supports it directly.
            // For simplicity, we'll refresh and if still not verified, show error.
            repository.refreshSession()
            _uiState.value = AuthUiState.Idle
        }
    }
}
