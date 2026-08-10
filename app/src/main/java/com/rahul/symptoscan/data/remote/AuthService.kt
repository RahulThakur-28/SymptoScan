package com.rahul.symptoscan.data.remote

import io.github.jan.supabase.auth.parseSessionFromUrl
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Service class to handle all authentication operations using Supabase.
 * Exposes clean Result-wrapped responses and handles underlying exceptions.
 */
class AuthService {

    private val auth = SupabaseClient.auth

    /**
     * Registers a new user with email and password.
     */
    suspend fun register(email: String, password: String, fullName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
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
     * Updates the password for the current authenticated user.
     */
    suspend fun updatePassword(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.updateUser {
                this.password = password
            }
            Unit
        }.onFailure { it.printStackTrace() }
            .mapError()
    }

    /**
     * Handles deep links by parsing session data from the provided URL.
     */
    suspend fun handleDeepLink(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val session = auth.parseSessionFromUrl(url)
            auth.importSession(session)
            // Fetch the user data associated with the session to ensure metadata is fresh
            auth.retrieveUserForCurrentSession(updateSession = true)
            Unit
        }.onFailure { it.printStackTrace() }
            .mapError()
    }

    /**
     * Extension to map Throwable into a more readable Result error.
     */
    private fun <T> Result<T>.mapError(): Result<T> {
        return if (isFailure) {
            val exception = exceptionOrNull()
            val message = when (exception) {
                is RestException -> {
                    val errorBody = exception.error
                    when {
                        errorBody.contains("user_already_exists", ignoreCase = true) || 
                        errorBody.contains("already registered", ignoreCase = true) -> 
                            "An account with this email already exists."
                        
                        errorBody.contains("invalid_credentials", ignoreCase = true) || 
                        errorBody.contains("Invalid login credentials", ignoreCase = true) -> 
                            "Incorrect email or password."
                        
                        errorBody.contains("Email not confirmed", ignoreCase = true) -> 
                            "Please verify your email before logging in."
                            
                        errorBody.contains("signup_disabled", ignoreCase = true) -> 
                            "Sign up is currently disabled."
                            
                        else -> exception.localizedMessage ?: "Server error: ${exception.error}"
                    }
                }
                is HttpRequestException -> "Network error. Please check your internet connection."
                else -> exception?.localizedMessage ?: "An unknown error occurred"
            }
            Result.failure(Exception(message))
        } else {
            this
        }
    }
}

