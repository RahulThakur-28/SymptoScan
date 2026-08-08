package com.rahul.symptoscan.presentation.assessment.state

import com.rahul.symptoscan.domain.model.AiQuestion
import com.rahul.symptoscan.domain.model.AssessmentResult
import com.rahul.symptoscan.domain.model.Symptom

data class AssessmentUiState(
    val symptoms: List<Symptom> = emptyList(),
    val searchQuery: String = "",
    val recentSymptoms: List<Symptom> = emptyList(),
    val selectedSymptoms: List<Symptom> = emptyList(),
    
    // Symptom Details
    val overallSeverity: Float = 1f,
    val painLevel: Float = 0f,
    val selectedDuration: String = "",
    val selectedFrequency: String = "",
    val selectedOnset: String = "",
    val bodyTemperature: Float = 37.0f,
    val additionalNotes: String = "",
    
    // AI Questions
    val questions: List<AiQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    
    val isAnalyzing: Boolean = false,
    val result: AssessmentResult? = null,
    val error: String? = null
) {
    val currentQuestion: AiQuestion? 
        get() = questions.getOrNull(currentQuestionIndex)
        
    val progress: Float
        get() = if (questions.isEmpty()) 0f else (currentQuestionIndex + 1).toFloat() / questions.size
}
