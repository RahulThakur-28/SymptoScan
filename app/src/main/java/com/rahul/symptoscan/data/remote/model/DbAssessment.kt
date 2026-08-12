package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbAssessment(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("status")
    val status: String = "in_progress",
    @SerialName("body_temperature")
    val bodyTemperature: Double? = null,
    @SerialName("additional_notes")
    val additionalNotes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null
)

@Serializable
data class DbAssessmentSymptom(
    @SerialName("id")
    val id: String? = null,
    @SerialName("assessment_id")
    val assessmentId: String,
    @SerialName("symptom_name")
    val symptomName: String,
    @SerialName("is_custom")
    val isCustom: Boolean = false,
    @SerialName("severity")
    val severity: Int? = null,
    @SerialName("pain_level")
    val painLevel: Int? = null,
    @SerialName("duration")
    val duration: String? = null,
    @SerialName("frequency")
    val frequency: String? = null,
    @SerialName("onset")
    val onset: String? = null
)

@Serializable
data class DbAssessmentQuestion(
    @SerialName("id")
    val id: String? = null,
    @SerialName("assessment_id")
    val assessmentId: String,
    @SerialName("question")
    val question: String,
    @SerialName("answer")
    val answer: String? = null,
    @SerialName("question_order")
    val questionOrder: Int
)

@Serializable
data class DbAssessmentResult(
    @SerialName("id")
    val id: String? = null,
    @SerialName("assessment_id")
    val assessmentId: String,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("possible_causes")
    val possibleCauses: List<String>? = null,
    @SerialName("recommendations")
    val recommendations: List<String>? = null,
    @SerialName("warning_signs")
    val warningSigns: List<String>? = null,
    @SerialName("urgency_level")
    val urgencyLevel: String? = null,
    @SerialName("disclaimer")
    val disclaimer: String? = null
)

@Serializable
data class DbAssessmentWithResult(
    val id: String,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("assessment_results") val result: DbAssessmentResult? = null
)
