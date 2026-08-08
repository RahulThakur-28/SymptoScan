package com.rahul.symptoscan.presentation.settings.state

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val pushNotifications: Boolean = true,
    val emailReports: Boolean = true,
    val marketingEmails: Boolean = false,
    val biometricEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = true,
    val currentLanguage: String = "English (United States)",
    val appVersion: String = "2.4.1 (Build 241)",
    val showDeleteDialog: Boolean = false
)
