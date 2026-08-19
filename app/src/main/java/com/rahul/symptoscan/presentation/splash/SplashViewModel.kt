package com.rahul.symptoscan.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _destination = MutableStateFlow<String?>(null)
    val destination = _destination.asStateFlow()

    init {
        startSplash()
    }

    private fun startSplash() {
        viewModelScope.launch {
            delay(2000) // 2 seconds delay as requested
            // In a real app, check SharedPreferences or DataStore for isFirstLaunch
            val isFirstLaunch = true 
            _destination.value = if (isFirstLaunch) "onboarding" else "login"
            _isReady.value = true
        }
    }
}
