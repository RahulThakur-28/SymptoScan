package com.rahul.symptoscan.core.di

import com.rahul.symptoscan.data.remote.AuthService
import com.rahul.symptoscan.data.repository.AssessmentRepository
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.ProfileRepository

/**
 * Manual Dependency Injection provider.
 */
object Injection {
    private val authService by lazy { AuthService() }
    
    val authRepository by lazy { AuthRepository(authService) }
    
    val assessmentRepository by lazy { AssessmentRepository() }
    
    val profileRepository by lazy { ProfileRepository() }
}
