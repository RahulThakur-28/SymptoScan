package com.rahul.symptoscan.presentation.auth.resetpassword.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.resetpassword.event.ResetPasswordEvent
import com.rahul.symptoscan.presentation.auth.resetpassword.state.ResetPasswordState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val repository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState<Unit>> = _uiState.asStateFlow()

    fun onEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.PasswordChanged -> {
                _state.update { it.copy(
                    password = event.password,
                    passwordError = if (event.password.length >= 8) null else "Password must be at least 8 characters"
                ) }
            }
            is ResetPasswordEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(
                    confirmPassword = event.password,
                    confirmPasswordError = if (event.password == it.password) null else "Passwords must match"
                ) }
            }
            is ResetPasswordEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is ResetPasswordEvent.ToggleConfirmPasswordVisibility -> {
                _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
            is ResetPasswordEvent.UpdateClicked -> {
                updatePassword()
            }
        }
    }

    private fun updatePassword() {
        if (!_state.value.isUpdateEnabled) return
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            val result = repository.updatePassword(_state.value.password)
            
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                _uiState.value = AuthUiState.Success(Unit)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Failed to update password")
            }
        }
    }
}
