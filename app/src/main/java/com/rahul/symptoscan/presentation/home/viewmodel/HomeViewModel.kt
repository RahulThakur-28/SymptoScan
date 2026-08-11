package com.rahul.symptoscan.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.HealthProfileRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.domain.model.AssessmentStatus
import com.rahul.symptoscan.domain.model.AssessmentSummary
import com.rahul.symptoscan.presentation.home.model.HomeUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository = Injection.authRepository,
    private val profileRepository: ProfileRepository = Injection.profileRepository,
    private val healthProfileRepository: HealthProfileRepository = Injection.healthProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val user = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            combine(
                profileRepository.getUserProfile(user.id),
                healthProfileRepository.getHealthProfile(user.id)
            ) { profile, healthProfile ->
                _uiState.update { it.copy(
                    userName = profile?.fullName ?: "User",
                    initials = profile?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.joinToString("")?.take(2) ?: "U",
                    isProfileComplete = healthProfile?.profileCompleted ?: false,
                    bmi = healthProfile?.heightCm?.let { h ->
                        healthProfile.weightKg?.let { w ->
                            val hMeters = h / 100.0
                            w / (hMeters * hMeters)
                        }
                    }
                ) }
            }.collect()
        }

        // Dummy data for remaining parts
        _uiState.update { it.copy(
            healthScore = 82,
            lastCheck = "3d ago",
            assessmentCount = 12,
            healthStatus = "Good health status",
            dailyHealthTip = "Drinking 8 glasses of water daily can improve cognitive function and reduce fatigue by up to 23%.",
            recentAssessments = listOf(
                AssessmentSummary("1", "Headache & Fatigue", "2 days ago", AssessmentStatus.Low, 28),
                AssessmentSummary("2", "Chest Discomfort", "5 days ago", AssessmentStatus.Moderate, 54),
                AssessmentSummary("3", "Seasonal Allergies", "1 week ago", AssessmentStatus.Low, 22)
            ),
            notificationCount = 3
        ) }
    }
}
