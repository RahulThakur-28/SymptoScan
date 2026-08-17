package com.rahul.symptoscan.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AiHealthProfileContext(
    val age: Int?,
    val biologicalSex: String?,
    val bloodGroup: String?,
    val heightCm: Double?,
    val weightKg: Double?,
    val allergies: String?,
    val existingConditions: String?,
    val currentMedicines: String?
)

@Serializable
data class AiAssessmentContext(
    val symptoms: List<String>,
    val description: String?,
    val bodyTemperature: Double?,
    val hasImage: Boolean,
    val healthProfile: AiHealthProfileContext?
)

@Serializable
data class AiQuestionAnswer(
    val question: String,
    val answer: String
)

@Serializable
data class CompleteAiAssessmentContext(
    val initialContext: AiAssessmentContext,
    val followUpAnswers: List<AiQuestionAnswer>
)
