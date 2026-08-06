package com.rahul.symptoscan.presentation.auth.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.register.event.RegisterEvent
import com.rahul.symptoscan.presentation.auth.register.state.RegisterState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState<Unit>> = _uiState.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged -> {
                _state.update { it.copy(
                    fullName = event.name,
                    fullNameError = if (event.name.isNotBlank()) null else "Name is required"
                ) }
            }
            is RegisterEvent.EmailChanged -> {
                _state.update { it.copy(
                    email = event.email,
                    emailError = if (validateEmail(event.email)) null else "Valid email is required"
                ) }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update { 
                    val error = if (event.password.length >= 8) null else "Password must be at least 8 characters"
                    it.copy(
                        password = event.password,
                        passwordError = error,
                        confirmPasswordError = if (it.confirmPassword.isEmpty() || event.password == it.confirmPassword) null else "Passwords must match"
                    )
                }
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(
                    confirmPassword = event.password,
                    confirmPasswordError = if (event.password == it.password) null else "Passwords must match"
                ) }
            }
            is RegisterEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is RegisterEvent.ToggleConfirmPasswordVisibility -> {
                _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
            is RegisterEvent.RegisterClicked -> {
                register()
            }
            else -> {}
        }
    }

    private fun register() {
        if (!_state.value.isRegisterEnabled) return
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            val result = repository.register(_state.value.email, _state.value.password)
            
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                _uiState.value = AuthUiState.Success(Unit)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
