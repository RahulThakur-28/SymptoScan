package com.rahul.symptoscan.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.local.PreferenceManager
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.presentation.settings.state.SettingsUiState
import com.rahul.symptoscan.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository = Injection.authRepository,
    private val preferenceManager: PreferenceManager = Injection.preferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val storedMode = preferenceManager.getThemeMode()
        val themeMode = ThemeMode.valueOf(storedMode)
        
        _uiState.update { it.copy(
            themeMode = themeMode,
            pushNotifications = preferenceManager.arePushNotificationsEnabled(),
            emailReports = preferenceManager.areEmailReportsEnabled()
        ) }
    }

    fun setThemeMode(mode: ThemeMode) {
        preferenceManager.setThemeMode(mode.name)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        val mode = if (enabled) ThemeMode.Dark else ThemeMode.Light
        setThemeMode(mode)
    }

    fun togglePushNotifications(enabled: Boolean) {
        preferenceManager.setPushNotifications(enabled)
        _uiState.update { it.copy(pushNotifications = enabled) }
    }

    fun toggleEmailReports(enabled: Boolean) {
        preferenceManager.setEmailReports(enabled)
        _uiState.update { it.copy(emailReports = enabled) }
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
