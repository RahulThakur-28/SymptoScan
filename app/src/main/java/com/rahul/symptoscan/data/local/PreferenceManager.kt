package com.rahul.symptoscan.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("symptoscan_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }

    private val _language = MutableStateFlow(prefs.getString("language", "English") ?: "English")
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
        _language.value = language
    }

    fun getLanguage(): String {
        return prefs.getString("language", "English") ?: "English"
    }

    fun setPushNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("push_notifications", enabled).apply()
    }

    fun arePushNotificationsEnabled(): Boolean {
        return prefs.getBoolean("push_notifications", true)
    }

    fun setEmailReports(enabled: Boolean) {
        prefs.edit().putBoolean("email_reports", enabled).apply()
    }

    fun areEmailReportsEnabled(): Boolean {
        return prefs.getBoolean("email_reports", true)
    }
}
