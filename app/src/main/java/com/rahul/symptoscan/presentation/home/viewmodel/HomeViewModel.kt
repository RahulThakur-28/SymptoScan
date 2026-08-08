package com.rahul.symptoscan.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import com.rahul.symptoscan.domain.model.AssessmentStatus
import com.rahul.symptoscan.domain.model.AssessmentSummary
import com.rahul.symptoscan.presentation.home.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Initialize with dummy data matching the reference
        _uiState.value = HomeUiState(
            userName = "Sarah Johnson",
            initials = "SJ",
            healthScore = 82,
            bmi = 22.4,
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
        )
    }
}
