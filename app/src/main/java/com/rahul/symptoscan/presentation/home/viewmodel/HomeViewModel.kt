package com.rahul.symptoscan.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AssessmentRepository
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.HealthProfileRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.domain.model.AssessmentStatus
import com.rahul.symptoscan.domain.model.AssessmentSummary
import com.rahul.symptoscan.domain.model.HealthProfile
import com.rahul.symptoscan.presentation.home.model.HomeUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
    private val authRepository: AuthRepository = Injection.authRepository,
    private val profileRepository: ProfileRepository = Injection.profileRepository,
    private val healthProfileRepository: HealthProfileRepository = Injection.healthProfileRepository,
    private val assessmentRepository: AssessmentRepository = Injection.assessmentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val healthTips = listOf(
        "Drinking 8 glasses of water daily can improve cognitive function and reduce fatigue.",
        "Getting 7-9 hours of sleep helps regulate immune function and mental clarity.",
        "Regular physical activity can reduce the risk of chronic diseases by up to 30%.",
        "A balanced diet rich in fiber supports digestive health and energy levels.",
        "Managing stress through mindfulness can lower blood pressure and improve heart health."
    )

    init {
        loadData()
    }

    fun onRefresh() {
        loadData(isRefresh = true)
    }

    fun loadData(isRefresh: Boolean = false) {
        val user = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            
            combine(
                profileRepository.getUserProfile(user.id),
                healthProfileRepository.getHealthProfile(user.id),
                assessmentRepository.getAssessmentHistory(),
                assessmentRepository.getLatestCompletedAssessment()
            ) { profile, healthProfile, history, latestAssessment ->
                val healthScore = assessmentRepository.calculateHealthScore(history)
                val bmi = calculateBmi(healthProfile)
                
                _uiState.update { it.copy(
                    userName = profile?.fullName ?: "User",
                    initials = profile?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.joinToString("")?.take(2) ?: "U",
                    isProfileComplete = healthProfile?.profileCompleted ?: false,
                    bmi = bmi,
                    assessmentCount = history.size,
                    recentAssessments = history.take(3),
                    lastCheck = latestAssessment?.createdAt?.let { 
                        com.rahul.symptoscan.core.utils.DateUtils.formatIsoToReadable(it).split(",").firstOrNull() ?: "No assessments"
                    } ?: "No assessments",
                    healthScore = healthScore,
                    healthStatus = deriveHealthStatus(healthScore, history),
                    dailyHealthTip = healthTips.random(),
                    notificationCount = 0,
                    isLoading = false,
                    isRefreshing = false
                ) }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load data") }
            }.collect()
        }
    }

    private fun calculateBmi(healthProfile: HealthProfile?): Double? {
        val h = healthProfile?.heightCm ?: return null
        val w = healthProfile.weightKg ?: return null
        if (h <= 0) return null
        val hMeters = h / 100.0
        return w / (hMeters * hMeters)
    }

    private fun deriveHealthStatus(score: Int, history: List<AssessmentSummary>): String {
        if (history.any { it.status == AssessmentStatus.High }) return "Attention needed"
        return when {
            score >= 80 -> "Good health status"
            score >= 60 -> "Fair health status"
            else -> "Needs improvement"
        }
    }
}
