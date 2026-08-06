package com.rahul.symptoscan.data.remote

import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service class to handle all authentication operations using Supabase.
 * Exposes clean Result-wrapped responses and handles underlying exceptions.
 */
class AuthService {

    private val auth = SupabaseClient.auth

    /**
     * Registers a new user with email and password.
     */
    suspend fun register(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Unit
        }.onFailure { it.printStackTrace() }
            .mapError()
    }

    /**
     * Logs in an existing user with email and password.
     */
    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Unit
        }.onFailure { it.printStackTrace() }
            .mapError()
    }

    /**
     * Logs out the currently authenticated user.
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signOut()
        }.onFailure { it.printStackTrace() }
            .mapError()
    }

    /**
     * Sends a password reset link to the specified email.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.resetPasswordForEmail(email)
        }.onFailure { it.printStackTrace() }
            .mapError()
    }

    /**
     * Retrieves the current user information if a session exists.
     */
    fun getCurrentUser(): UserInfo? {
        return auth.currentUserOrNull()
    }
    /**
     * Refreshes the current session and fetches the latest user data from the server.
     * This is essential to detect changes in email verification status.
     */
    suspend fun refreshSession(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.retrieveUserForCurrentSession(updateSession = true)
            Unit
        }.onFailure { it.printStackTrace() }
            .mapError()
    }

    /**
     * Checks if a user is currently logged in.
     */
    fun isLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }

    /**
     * Extension to map Throwable into a more readable Result error.
     */
    private fun <T> Result<T>.mapError(): Result<T> {
        return if (isFailure) {
            val message = when (val exception = exceptionOrNull()) {
                is RestException -> exception.error
                is HttpRequestException -> "Network error. Please check your internet connection."
                else -> exception?.localizedMessage ?: "An unknown error occurred"
            }
            Result.failure(Exception(message))
        } else {
            this
        }
    }
}
