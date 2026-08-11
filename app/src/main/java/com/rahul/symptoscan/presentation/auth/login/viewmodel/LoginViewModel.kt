package com.rahul.symptoscan.presentation.auth.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.core.utils.AuthValidator
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.login.event.LoginEvent
import com.rahul.symptoscan.presentation.auth.login.state.LoginState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = Injection.authRepository,
    private val profileRepository: ProfileRepository = Injection.profileRepository
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
                val email = event.email.trim()
                _state.update { it.copy(
                    email = email,
                    emailError = if (AuthValidator.validateEmail(email)) null else "Invalid email address"
                ) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(
                    password = event.password,
                    passwordError = null
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
            
            result.onSuccess {
                val profileResult = profileRepository.ensureUserProfile()
                if (profileResult.isFailure) {
                    val detailedError = profileResult.exceptionOrNull()?.message ?: "Unknown Error"
                    if (com.rahul.symptoscan.BuildConfig.DEBUG) {
                        _uiState.value = AuthUiState.Error("Profile Error: $detailedError")
                    } else {
                        _uiState.value = AuthUiState.Error("Unable to create your profile. Please try again.")
                    }
                    return@launch
                }

                _uiState.value = AuthUiState.Idle
                
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
        val email = _state.value.email.trim()
        val password = _state.value.password

        if (!AuthValidator.validateEmail(email)) {
            _state.update { it.copy(emailError = "Invalid email address") }
            return
        }
        if (password.isEmpty()) {
            _state.update { it.copy(passwordError = "Password cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _state.update { it.copy(isLoading = true) }
            
            val result = repository.login(email, password)
            
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                // Important: retrieve user to get fresh metadata (like email_confirmed_at)
                repository.refreshSession()
                
                if (repository.isEmailVerified()) {
                    val profileResult = profileRepository.ensureUserProfile()
                    if (profileResult.isFailure) {
                        val detailedError = profileResult.exceptionOrNull()?.message ?: "Unknown Error"
                        if (com.rahul.symptoscan.BuildConfig.DEBUG) {
                            _uiState.value = AuthUiState.Error("Profile Error: $detailedError")
                        } else {
                            _uiState.value = AuthUiState.Error("Unable to create your profile. Please try again.")
                        }
                        return@launch
                    }

                    if (repository.isProfileCompleted()) {
                        _navigationEvent.emit(LoginNavigation.NavigateToHome)
                    } else {
                        _navigationEvent.emit(LoginNavigation.NavigateToProfile)
                    }
                } else {
                    _uiState.value = AuthUiState.EmailNotVerified
                }
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Incorrect email or password.")
            }
        }
    }


    sealed class LoginNavigation {
        object NavigateToHome : LoginNavigation()
        object NavigateToVerification : LoginNavigation()
        object NavigateToResetPassword : LoginNavigation()
        object NavigateToProfile : LoginNavigation()
    }
}
