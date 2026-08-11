package com.rahul.symptoscan.presentation.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SplashViewModel handles the initial routing logic based on session and profile status.
 */
class SplashViewModel(
    private val repository: AuthRepository = Injection.authRepository,
    private val profileRepository: ProfileRepository = Injection.profileRepository
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Idle)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        startStartupSequence()
    }

    private fun startStartupSequence() {
        viewModelScope.launch {
            // Branding visibility window
            delay(1000) 
            checkAuthentication()
        }
    }

    private suspend fun checkAuthentication() {
        try {
            if (!repository.isLoggedIn()) {
                _uiState.update { SplashUiState.NoSession }
                _navigationState.update { SplashNavigationState.NavigateToLogin }
                return
            }

            // Refresh session to ensure we have the latest user data (like verification status)
            repository.refreshSession()

            // Session exists, check verification
            if (!repository.isEmailVerified()) {
                _uiState.update { SplashUiState.Unverified }
                _navigationState.update { SplashNavigationState.NavigateToVerification }
                return
            }

            // Verified, ensure public.profiles row exists
            val profileResult = profileRepository.ensureUserProfile()
            if (profileResult.isFailure) {
                _uiState.update { SplashUiState.Error("Profile Error: ${profileResult.exceptionOrNull()?.message}") }
                return
            }

            // Check health profile completion
            if (!repository.isProfileCompleted()) {
                _uiState.update { SplashUiState.ProfileIncomplete }
                _navigationState.update { SplashNavigationState.NavigateToProfile }
                return
            }

            // Everything ready
            _uiState.update { SplashUiState.Authenticated }
            _navigationState.update { SplashNavigationState.NavigateToHome }
        } catch (e: Exception) {
            _uiState.update { SplashUiState.Error(e.message ?: "Startup Error") }
        }
    }

    fun onNavigationHandled() {
        _navigationState.update { SplashNavigationState.Idle }
    }
}

sealed class SplashUiState {
    data object Loading : SplashUiState()
    data object NoSession : SplashUiState()
    data object Unverified : SplashUiState()
    data object ProfileIncomplete : SplashUiState()
    data object Authenticated : SplashUiState()
    data class Error(val message: String) : SplashUiState()
}

sealed class SplashNavigationState {
    data object Idle : SplashNavigationState()
    data object NavigateToOnboarding : SplashNavigationState()
    data object NavigateToLogin : SplashNavigationState()
    data object NavigateToVerification : SplashNavigationState()
    data object NavigateToProfile : SplashNavigationState()
    data object NavigateToHome : SplashNavigationState()
}
