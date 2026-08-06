package com.rahul.symptoscan.presentation.auth.common

/**
 * Universal UI states for Authentication features.
 */
sealed class AuthUiState<out T> {
    object Idle : AuthUiState<Nothing>()
    object Loading : AuthUiState<Nothing>()
    data class Success<out T>(val data: T) : AuthUiState<T>()
    data class Error(val message: String) : AuthUiState<Nothing>()
    object EmailNotVerified : AuthUiState<Nothing>()
}
