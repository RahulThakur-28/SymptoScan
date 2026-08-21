package com.rahul.symptoscan.presentation.auth.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.core.utils.AuthValidator
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
                val name = event.name.trim()
                _state.update { it.copy(
                    fullName = name,
                    fullNameError = if (name.length >= 2) null else "Enter your full name"
                ) }
            }
            is RegisterEvent.EmailChanged -> {
                val email = event.email.trim()
                _state.update { it.copy(
                    email = email,
                    emailError = if (AuthValidator.validateEmail(email)) null else "Valid email is required"
                ) }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update { 
                    it.copy(
                        password = event.password,
                        passwordError = null, // Handled progressively in UI
                        confirmPasswordError = if (it.confirmPassword.isEmpty() || event.password == it.confirmPassword) null else "Passwords do not match"
                    )
                }
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(
                    confirmPassword = event.password,
                    confirmPasswordError = if (event.password == it.password) null else "Passwords do not match"
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
        val fullName = _state.value.fullName.trim()
        val email = _state.value.email.trim()
        val password = _state.value.password
        val confirmPassword = _state.value.confirmPassword

        if (fullName.length < 2) {
            _state.update { it.copy(fullNameError = "Enter your full name") }
            return
        }
        if (!AuthValidator.validateEmail(email)) {
            _state.update { it.copy(emailError = "Valid email is required") }
            return
        }
        val passValidation = AuthValidator.validatePassword(password)
        if (!passValidation.isValid) {
            _state.update { it.copy(passwordError = "Please meet all password requirements") }
            return
        }
        if (password != confirmPassword) {
            _state.update { it.copy(confirmPasswordError = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            // Pass fullName during registration
            val result = repository.register(email, password, fullName)
            
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                _uiState.value = AuthUiState.Success(Unit)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(com.rahul.symptoscan.core.utils.ErrorUtils.getUserFriendlyMessage(error))
            }
        }
    }

}
