package com.rahul.symptoscan.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.presentation.settings.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _uiState.update { it.copy(pushNotifications = enabled) }
    }

    fun toggleEmailReports(enabled: Boolean) {
        _uiState.update { it.copy(emailReports = enabled) }
    }

    fun toggleMarketingEmails(enabled: Boolean) {
        _uiState.update { it.copy(marketingEmails = enabled) }
    }

    fun toggleBiometric(enabled: Boolean) {
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }

    fun toggleCloudSync(enabled: Boolean) {
        _uiState.update { it.copy(cloudSyncEnabled = enabled) }
    }

    fun onDeleteAccountClicked() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDeleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            // In a real app, call repository.deleteAccount()
            authRepository.logout()
            onDeleted()
        }
    }
}
