package com.rahul.symptoscan.presentation.auth.forgotpassword.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.core.utils.AuthValidator
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
                val email = event.email.trim()
                _state.update { it.copy(
                    email = email,
                    emailError = if (AuthValidator.validateEmail(email)) null else "Invalid email address"
                ) }
            }
            is ForgotPasswordEvent.SubmitClicked -> submit()
        }
    }

    private fun submit() {
        val email = _state.value.email.trim()
        if (!AuthValidator.validateEmail(email)) {
            _state.update { it.copy(emailError = "Invalid email address") }
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            val result = repository.forgotPassword(email)
            
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                // Neutral success message is handled in Composable
                _uiState.value = AuthUiState.Success(Unit)
            }.onFailure {
                // Neutral wording: even if failure, we might want to show success to prevent email enumeration
                // but usually for network errors we show error.
                // Supabase forgotPassword might not fail if email doesn't exist depending on config.
                _uiState.value = AuthUiState.Success(Unit) 
            }
        }
    }
}
