package com.rahul.symptoscan.core.di

import android.content.Context
import com.rahul.symptoscan.data.local.PreferenceManager
import com.rahul.symptoscan.data.remote.AuthService
import com.rahul.symptoscan.data.repository.*

/**
 * Manual Dependency Injection provider.
 */
object Injection {
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private val authService by lazy { AuthService() }
    
    val authRepository by lazy { AuthRepository(authService) }
    
    val assessmentRepository by lazy { AssessmentRepository() }
    
    val profileRepository by lazy { ProfileRepository() }

    val healthProfileRepository by lazy { HealthProfileRepository() }

    val healthAssistantRepository by lazy { HealthAssistantRepository() }

    val preferenceManager by lazy { PreferenceManager(applicationContext) }
}
