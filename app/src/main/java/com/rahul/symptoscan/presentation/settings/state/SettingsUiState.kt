package com.rahul.symptoscan.presentation.settings.state

import com.rahul.symptoscan.ui.theme.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.System,
    val isDarkMode: Boolean = false, // Derived or used for simple toggles
    val pushNotifications: Boolean = true,
    val emailReports: Boolean = true,
    val appVersion: String = "2.4.1 (Build 241)",
    val showDeleteDialog: Boolean = false
)
