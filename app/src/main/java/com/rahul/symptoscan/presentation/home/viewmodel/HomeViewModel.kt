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
import java.text.SimpleDateFormat
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
        loadData()
    }

    fun loadData() {
        val user = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            combine(
                profileRepository.getUserProfile(user.id),
                healthProfileRepository.getHealthProfile(user.id),
                assessmentRepository.getAssessmentHistory()
            ) { profile, healthProfile, history ->
                val bmi = calculateBmi(healthProfile)
                val healthScore = calculateHealthScore(history, healthProfile)
                
                _uiState.update { it.copy(
                    userName = profile?.fullName ?: "User",
                    initials = profile?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.joinToString("")?.take(2) ?: "U",
                    isProfileComplete = healthProfile?.profileCompleted ?: false,
                    bmi = bmi,
                    assessmentCount = history.size,
                    recentAssessments = history.take(3),
                    lastCheck = history.firstOrNull()?.let { formatTime(it.time) } ?: "No assessments",
                    healthScore = healthScore,
                    healthStatus = deriveHealthStatus(healthScore, history),
                    dailyHealthTip = healthTips.random(),
                    notificationCount = 0, // No real notification source yet
                    isLoading = false
                ) }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load data") }
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

    private fun calculateHealthScore(history: List<AssessmentSummary>, healthProfile: HealthProfile?): Int {
        var score = 100
        
        // Deduction for poor assessment results
        history.forEach { assessment ->
            when (assessment.status) {
                AssessmentStatus.High -> score -= 15
                AssessmentStatus.Moderate -> score -= 5
                else -> {}
            }
        }
        
        // Deduction for incomplete profile
        if (healthProfile?.profileCompleted != true) score -= 10
        
        // Deduction for unhealthy BMI (very simple logic)
        val bmi = calculateBmi(healthProfile)
        if (bmi != null) {
            if (bmi < 18.5 || bmi > 25.0) score -= 5
            if (bmi > 30.0) score -= 5
        }

        return score.coerceIn(0, 100)
    }

    private fun deriveHealthStatus(score: Int, history: List<AssessmentSummary>): String {
        if (history.any { it.status == AssessmentStatus.High }) return "Attention needed"
        return when {
            score >= 80 -> "Good health status"
            score >= 60 -> "Fair health status"
            else -> "Needs improvement"
        }
    }

    private fun formatTime(isoString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoString.substring(0, 19)) ?: return "Recently"
            
            val now = Calendar.getInstance()
            val diff = now.timeInMillis - date.time
            
            when {
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> "${diff / 86400000}d ago"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Recently"
        }
    }
}
