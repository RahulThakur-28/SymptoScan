package com.rahul.symptoscan.presentation.auth.forgotpassword.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.forgotpassword.event.ForgotPasswordEvent
import com.rahul.symptoscan.presentation.auth.forgotpassword.state.ForgotPasswordState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState<Unit>> = _uiState.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.update { it.copy(
                    email = event.email,
                    emailError = if (validateEmail(event.email)) null else "Invalid email address"
                ) }
            }
            is ForgotPasswordEvent.SubmitClicked -> submit()
        }
    }

    private fun submit() {
        if (!_state.value.isSubmitEnabled) return
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            val result = repository.forgotPassword(_state.value.email)
            
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                _uiState.value = AuthUiState.Success(Unit)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Failed to send reset email")
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
