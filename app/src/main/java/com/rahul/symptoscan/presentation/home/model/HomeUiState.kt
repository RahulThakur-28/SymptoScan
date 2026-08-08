package com.rahul.symptoscan.presentation.home.model

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
    val notificationCount: Int = 0
)

data class AssessmentSummary(
    val id: String,
    val title: String,
    val time: String,
    val status: AssessmentStatus,
    val score: Int
)

enum class AssessmentStatus {
    Low, Moderate, High
}
