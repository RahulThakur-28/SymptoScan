package com.rahul.symptoscan.presentation.assessment.state

import com.rahul.symptoscan.data.remote.model.DbAssessmentQuestion
import com.rahul.symptoscan.data.remote.model.DbAssessmentResult
import com.rahul.symptoscan.domain.model.Symptom

data class AssessmentUiState(
    val assessmentId: String? = null,
    val searchQuery: String = "",
    val symptoms: List<Symptom> = emptyList(),
    val recentSymptoms: List<Symptom> = emptyList(),
    val selectedSymptoms: List<Symptom> = emptyList(),
    val symptomDetails: Map<String, SymptomDetails> = emptyMap(),
    val bodyTemperature: Double = 98.6,
    val additionalNotes: String = "",
    val selectedImageUri: android.net.Uri? = null,
    val isImageUploading: Boolean = false,
    val questions: List<DbAssessmentQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val result: DbAssessmentResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val progress: Float = 0f
)

data class SymptomDetails(
    val severity: Int = 5,
    val painLevel: Int = 0,
    val duration: String = "Today",
    val frequency: String = "Occasional",
    val onset: String = "Gradual"
)
