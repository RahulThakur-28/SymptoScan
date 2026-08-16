package com.rahul.symptoscan.domain.model

data class AssessmentSummary(
    val id: String,
    val title: String,
    val time: String,
    val status: AssessmentStatus,
    val score: Int? = null,
    val symptoms: List<String> = emptyList(),
    val hasImage: Boolean = false
)

enum class AssessmentStatus {
    Low, Moderate, High, Pending
}
