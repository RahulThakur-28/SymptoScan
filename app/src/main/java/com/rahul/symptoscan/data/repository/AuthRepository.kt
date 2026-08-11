package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.data.remote.AuthService
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.firstOrNull

/**
 * Repository implementation for Authentication.
 * Acts as a clean interface between the UI/ViewModel and the remote data source.
 */
class AuthRepository(private val authService: AuthService) {

    suspend fun register(email: String, password: String, fullName: String): Result<Unit> {
        return authService.register(email, password, fullName)
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

    suspend fun updatePassword(password: String): Result<Unit> {
        return authService.updatePassword(password)
    }

    suspend fun handleDeepLink(url: String): Result<Unit> {
        return authService.handleDeepLink(url)
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
     * Fallback to confirmedAt for broader confirmation status.
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun isEmailVerified(): Boolean {
        val user = authService.getCurrentUser()
        return (user?.emailConfirmedAt != null) || (user?.confirmedAt != null)
    }

    /**
     * Checks if the user has completed the basic health profile.
     */
    suspend fun isProfileCompleted(): Boolean {
        val user = authService.getCurrentUser() ?: return false
        val healthProfile = com.rahul.symptoscan.core.di.Injection.healthProfileRepository.getHealthProfile(user.id)
            .firstOrNull()
        
        // Basic profile is complete if the row exists and has minimal info
        return healthProfile != null && healthProfile.dateOfBirth != null
    }
}
