package com.rahul.symptoscan.presentation.auth.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Shared AuthViewModel for operations common across multiple screens, like logout.
 */
class AuthViewModel(
    private val repository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<AuthNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _navigationEvent.emit(AuthNavigation.NavigateToLogin)
        }
    }

    sealed class AuthNavigation {
        object NavigateToLogin : AuthNavigation()
    }
}
