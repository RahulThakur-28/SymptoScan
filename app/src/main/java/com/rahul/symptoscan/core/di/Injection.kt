package com.rahul.symptoscan.core.di

import com.rahul.symptoscan.data.remote.AuthService
import com.rahul.symptoscan.data.repository.AuthRepository

/**
 * Manual Dependency Injection provider.
 */
object Injection {
    private val authService by lazy { AuthService() }
    
    val authRepository by lazy { AuthRepository(authService) }
}
