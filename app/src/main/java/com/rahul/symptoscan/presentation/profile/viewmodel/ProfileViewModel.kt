package com.rahul.symptoscan.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.local.PreferenceManager
import com.rahul.symptoscan.data.repository.AssessmentRepository
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.EmergencyContactRepository
import com.rahul.symptoscan.data.repository.HealthProfileRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.domain.model.EmergencyContact
import com.rahul.symptoscan.presentation.profile.state.ProfileUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository = Injection.profileRepository,
    private val healthProfileRepository: HealthProfileRepository = Injection.healthProfileRepository,
    private val emergencyRepository: EmergencyContactRepository = Injection.emergencyContactRepository,
    private val assessmentRepository: AssessmentRepository = Injection.assessmentRepository,
    private val authRepository: AuthRepository = Injection.authRepository,
    private val preferenceManager: PreferenceManager = Injection.preferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun onRefresh() {
        loadProfileData(isRefresh = true)
    }

    @OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class, ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
    fun loadProfileData(isRefresh: Boolean = false) {
        val currentUser = authRepository.getCurrentUser() ?: return
        val userId = currentUser.id
        val email = currentUser.email ?: ""
        
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            
            combine(
                profileRepository.getUserProfile(userId),
                healthProfileRepository.getHealthProfile(userId),
                flow { emit(emergencyRepository.getEmergencyContact().getOrNull()) },
                assessmentRepository.getAssessmentHistory()
            ) { baseProfile: com.rahul.symptoscan.domain.model.UserProfile?, 
                healthProfile: com.rahul.symptoscan.domain.model.HealthProfile?, 
                emergencyContact: com.rahul.symptoscan.domain.model.EmergencyContact?,
                history: List<com.rahul.symptoscan.domain.model.AssessmentSummary> ->
                
                if (baseProfile == null) return@combine null

                val healthScore = calculateHealthScore(history, healthProfile)

                baseProfile.copy(
                    email = email,
                    isVerified = currentUser.emailConfirmedAt != null,
                    assessmentCount = history.size,
                    healthScore = healthScore,
                    dob = healthProfile?.dateOfBirth,
                    gender = healthProfile?.biologicalSex,
                    bloodGroup = healthProfile?.bloodGroup,
                    height = healthProfile?.heightCm,
                    weight = healthProfile?.weightKg,
                    allergies = healthProfile?.allergies,
                    conditions = healthProfile?.medicalConditions,
                    medications = healthProfile?.medications,
                    isProfileComplete = healthProfile?.profileCompleted ?: false,
                    emergencyContact = emergencyContact
                )
            }.flatMapLatest { fullProfile ->
                if (fullProfile == null) {
                    flowOf(null to emptyList<com.rahul.symptoscan.domain.model.Achievement>())
                } else {
                    profileRepository.getAchievements(fullProfile.assessmentCount, fullProfile.isProfileComplete)
                        .map { fullProfile to it }
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load profile") }
            }.collect { (fullProfile, achievements) ->
                if (fullProfile == null) {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "Profile not found") }
                } else {
                    _uiState.update { 
                        it.copy(
                            user = fullProfile,
                            achievements = achievements,
                            currentLanguage = preferenceManager.getLanguage(),
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    private fun calculateHealthScore(
        history: List<com.rahul.symptoscan.domain.model.AssessmentSummary>, 
        healthProfile: com.rahul.symptoscan.domain.model.HealthProfile?
    ): Int {
        var score = 100
        
        // Deduction for poor assessment results
        history.forEach { assessment ->
            when (assessment.status) {
                com.rahul.symptoscan.domain.model.AssessmentStatus.High -> score -= 15
                com.rahul.symptoscan.domain.model.AssessmentStatus.Moderate -> score -= 5
                else -> {}
            }
        }
        
        // Deduction for incomplete profile
        if (healthProfile?.profileCompleted != true) score -= 10
        
        // Deduction for unhealthy BMI
        val h = healthProfile?.heightCm ?: 0.0
        val w = healthProfile?.weightKg ?: 0.0
        if (h > 0 && w > 0) {
            val hMeters = h / 100.0
            val bmi = w / (hMeters * hMeters)
            if (bmi < 18.5 || bmi > 25.0) score -= 5
            if (bmi > 30.0) score -= 5
        }

        return score.coerceIn(0, 100)
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }
}
