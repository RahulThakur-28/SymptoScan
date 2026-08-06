package com.rahul.symptoscan.presentation.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SplashViewModel handles the initial routing logic based on session and profile status.
 */
class SplashViewModel(
    private val repository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Idle)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    init {
        startSplashTimer()
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            // Minimum branding visibility
            delay(2000)
            checkSession()
        }
    }

    private fun checkSession() {
        if (!repository.isLoggedIn()) {
            val isFirstLaunch = checkFirstLaunch()
            _navigationState.value = if (isFirstLaunch) {
                SplashNavigationState.NavigateToOnboarding
            } else {
                SplashNavigationState.NavigateToLogin
            }
            return
        }

        // Session exists, check verification
        if (!repository.isEmailVerified()) {
            _navigationState.value = SplashNavigationState.NavigateToVerification
            return
        }

        // Verified, check profile completion
        if (!isProfileCompleted()) {
            _navigationState.value = SplashNavigationState.NavigateToProfile
            return
        }

        // Everything ready
        _navigationState.value = SplashNavigationState.NavigateToHome
    }

    private fun checkFirstLaunch(): Boolean {
        // Mocking for now. In production, use DataStore/SharedPreferences
        return false
    }

    private fun isProfileCompleted(): Boolean {
        // Mocking for now. In production, check if user has a record in 'profiles' table
        return false
    }
}

sealed class SplashNavigationState {
    data object Idle : SplashNavigationState()
    data object NavigateToOnboarding : SplashNavigationState()
    data object NavigateToLogin : SplashNavigationState()
    data object NavigateToVerification : SplashNavigationState()
    data object NavigateToProfile : SplashNavigationState()
    data object NavigateToHome : SplashNavigationState()
}
