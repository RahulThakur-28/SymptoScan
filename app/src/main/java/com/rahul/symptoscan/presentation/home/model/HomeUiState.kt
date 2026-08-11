package com.rahul.symptoscan.presentation.home.model

import com.rahul.symptoscan.domain.model.AssessmentSummary

data class HomeUiState(
    val userName: String = "",
    val initials: String = "",
    val healthScore: Int = 0,
    val bmi: Double? = null,
    val lastCheck: String? = null,
    val assessmentCount: Int = 0,
    val healthStatus: String = "",
    val dailyHealthTip: String = "",
    val recentAssessments: List<AssessmentSummary> = emptyList(),
    val notificationCount: Int = 0,
    val isProfileComplete: Boolean = true
)
