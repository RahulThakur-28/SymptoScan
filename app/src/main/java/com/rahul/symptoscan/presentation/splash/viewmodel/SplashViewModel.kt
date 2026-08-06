package com.rahul.symptoscan.presentation.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SplashViewModel handles the splash timing and navigation state.
 * Refined for production use with StateFlow.
 */
class SplashViewModel : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Idle)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    init {
        startSplashTimer()
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            // Total splash duration: 2000ms
            delay(2000)
            
            val isFirstLaunch = checkFirstLaunch()
            _navigationState.value = if (isFirstLaunch) {
                SplashNavigationState.NavigateToOnboarding
            } else {
                SplashNavigationState.NavigateToLogin
            }
        }
    }

    private fun checkFirstLaunch(): Boolean {
        // Placeholder for real logic (e.g., DataStore)
        return true
    }
}

sealed class SplashNavigationState {
    data object Idle : SplashNavigationState()
    data object NavigateToOnboarding : SplashNavigationState()
    data object NavigateToLogin : SplashNavigationState()
}