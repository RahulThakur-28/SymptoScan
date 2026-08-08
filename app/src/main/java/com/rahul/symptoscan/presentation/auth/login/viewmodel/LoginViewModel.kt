package com.rahul.symptoscan.presentation.auth.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.login.event.LoginEvent
import com.rahul.symptoscan.presentation.auth.login.state.LoginState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState<Unit>>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState<Unit>> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<LoginNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(
                    email = event.email,
                    emailError = if (validateEmail(event.email)) null else "Invalid email address"
                ) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(
                    password = event.password,
                    passwordError = if (event.password.length >= 8) null else "Password must be at least 8 characters"
                ) }
            }
            is LoginEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is LoginEvent.LoginClicked -> {
                login()
            }
            else -> {}
        }
    }

    fun handleDeepLink(url: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.handleDeepLink(url)
            _uiState.value = AuthUiState.Idle
            
            result.onSuccess {
                // Check if it's a recovery link
                if (url.contains("type=recovery")) {
                    _navigationEvent.emit(LoginNavigation.NavigateToResetPassword)
                } else if (repository.isEmailVerified()) {
                    if (repository.isProfileCompleted()) {
                        _navigationEvent.emit(LoginNavigation.NavigateToHome)
                    } else {
                        _navigationEvent.emit(LoginNavigation.NavigateToProfile)
                    }
                } else {
                    _navigationEvent.emit(LoginNavigation.NavigateToVerification)
                }
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Invalid or expired link")
            }
        }
    }

    private fun login() {
        if (!_state.value.isSignInEnabled) return
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            val result = repository.login(_state.value.email, _state.value.password)
            
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                if (repository.isEmailVerified()) {
                    if (repository.isProfileCompleted()) {
                        _navigationEvent.emit(LoginNavigation.NavigateToHome)
                    } else {
                        _navigationEvent.emit(LoginNavigation.NavigateToProfile)
                    }
                } else {
                    _uiState.value = AuthUiState.EmailNotVerified
                }
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Unknown error occurred")
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    sealed class LoginNavigation {
        object NavigateToHome : LoginNavigation()
        object NavigateToVerification : LoginNavigation()
        object NavigateToResetPassword : LoginNavigation()
        object NavigateToProfile : LoginNavigation()
    }
}
