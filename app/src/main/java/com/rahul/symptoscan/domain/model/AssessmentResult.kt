package com.rahul.symptoscan.domain.model

data class AssessmentResult(
    val riskScore: Int,
    val riskLevel: String, // e.g., "Low Risk", "Moderate Risk", "High Risk"
    val date: String,
    val symptomCount: Int,
    val aiExplanation: String,
    val conditions: List<AssessmentCondition>,
    val recommendations: List<Recommendation>,
    val specialist: RecommendedSpecialist
)

data class AssessmentCondition(
    val id: String,
    val name: String,
    val severity: String,
    val confidence: Int, // Percentage
    val icon: String
)

data class Recommendation(
    val id: String,
    val title: String,
    val icon: String
)

data class RecommendedSpecialist(
    val title: String,
    val description: String
)
