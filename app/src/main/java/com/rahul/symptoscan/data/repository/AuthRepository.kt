package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.data.remote.AuthService
import io.github.jan.supabase.auth.user.UserInfo

/**
 * Repository implementation for Authentication.
 * Acts as a clean interface between the UI/ViewModel and the remote data source.
 */
class AuthRepository(private val authService: AuthService) {

    suspend fun register(email: String, password: String): Result<Unit> {
        return authService.register(email, password)
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return authService.login(email, password)
    }

    suspend fun logout(): Result<Unit> {
        return authService.logout()
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return authService.sendPasswordReset(email)
    }

    fun getCurrentUser(): UserInfo? {
        return authService.getCurrentUser()
    }

    suspend fun refreshSession(): Result<Unit> {
        return authService.refreshSession()
    }

    fun isLoggedIn(): Boolean {
        return authService.isLoggedIn()
    }
    
    /**
     * Checks if the user's email is verified.
     * Uses emailConfirmedAt which is specifically for email verification.
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun isEmailVerified(): Boolean {
        return authService.getCurrentUser()?.emailConfirmedAt != null
    }
}
