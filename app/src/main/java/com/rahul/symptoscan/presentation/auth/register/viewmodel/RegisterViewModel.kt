package com.rahul.symptoscan.presentation.auth.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.presentation.auth.register.event.RegisterEvent
import com.rahul.symptoscan.presentation.auth.register.state.RegisterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged -> {
                _state.update { it.copy(
                    fullName = event.name,
                    fullNameError = if (event.name.isNotBlank()) null else "Name cannot be empty"
                ) }
            }
            is RegisterEvent.EmailChanged -> {
                _state.update { it.copy(
                    email = event.email,
                    emailError = if (validateEmail(event.email)) null else "Invalid email address"
                ) }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update { 
                    val error = if (event.password.length >= 6) null else "Password must be at least 6 characters"
                    it.copy(
                        password = event.password,
                        passwordError = error,
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
        if (!_state.value.isRegisterEnabled) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(2000)
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}