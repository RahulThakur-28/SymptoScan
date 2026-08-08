package com.rahul.symptoscan.presentation.assessment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.domain.model.*
import com.rahul.symptoscan.presentation.assessment.state.AssessmentUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssessmentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState: StateFlow<AssessmentUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val initialSymptoms = listOf(
            Symptom("1", "Headache", "🤕", "General"),
            Symptom("2", "Fever", "🤒", "General"),
            Symptom("3", "Cough", "😷", "Respiratory"),
            Symptom("4", "Fatigue", "😴", "General"),
            Symptom("5", "Nausea", "🤢", "Digestive"),
            Symptom("6", "Chest Pain", "🫀", "Cardiovascular"),
            Symptom("7", "Shortness of Breath", "🫁", "Respiratory"),
            Symptom("8", "Sore Throat", "👄", "Respiratory"),
            Symptom("9", "Back Pain", "🧍", "Musculoskeletal"),
            Symptom("10", "Dizziness", "😵‍💫", "General"),
            Symptom("11", "Joint Pain", "🦴", "Musculoskeletal"),
            Symptom("12", "Rash", "🧴", "Skin")
        )

        _uiState.update { it.copy(
            symptoms = initialSymptoms,
            recentSymptoms = initialSymptoms.take(3),
            questions = listOf(
                AiQuestion("q1", "Have you experienced these symptoms before?", listOf("Never", "Rarely (1–2 times)", "Sometimes", "Often", "Always")),
                AiQuestion("q2", "Does anything make the symptoms better or worse?", listOf("Yes, rest helps", "Yes, movement makes it worse", "No, it's constant", "I'm not sure")),
                AiQuestion("q3", "Are you taking any medications for this?", listOf("Yes, prescription", "Yes, over-the-counter", "No", "Just started today")),
                AiQuestion("q4", "Have you been in contact with anyone ill recently?", listOf("Yes, family/friends", "Yes, at work", "No", "Possibly")),
                AiQuestion("q5", "How much is this affecting your daily activities?", type = QuestionType.SCALE)
            )
        ) }
    }

    fun onSymptomToggle(symptomId: String) {
        _uiState.update { state ->
            val updatedSymptoms = state.symptoms.map {
                if (it.id == symptomId) it.copy(isSelected = !it.isSelected) else it
            }
            state.copy(
                symptoms = updatedSymptoms,
                selectedSymptoms = updatedSymptoms.filter { it.isSelected }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSeverityChange(value: Float) {
        _uiState.update { it.copy(overallSeverity = value) }
    }

    fun onPainLevelChange(value: Float) {
        _uiState.update { it.copy(painLevel = value) }
    }

    fun onDurationSelect(duration: String) {
        _uiState.update { it.copy(selectedDuration = duration) }
    }

    fun onFrequencySelect(frequency: String) {
        _uiState.update { it.copy(selectedFrequency = frequency) }
    }

    fun onOnsetSelect(onset: String) {
        _uiState.update { it.copy(selectedOnset = onset) }
    }

    fun onTemperatureChange(value: Float) {
        _uiState.update { it.copy(bodyTemperature = value) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(additionalNotes = notes) }
    }

    fun onAnswerSelect(questionId: String, answer: String) {
        val currentAnswers = _uiState.value.answers.toMutableMap()
        currentAnswers[questionId] = answer
        _uiState.update { it.copy(answers = currentAnswers) }
    }

    fun onNextQuestion() {
        if (_uiState.value.currentQuestionIndex < _uiState.value.questions.size - 1) {
            _uiState.update { it.copy(currentQuestionIndex = it.currentQuestionIndex + 1) }
        } else {
            analyzeSymptoms()
        }
    }

    private fun analyzeSymptoms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            delay(3000) // Simulating AI Analysis

            val dummyResult = AssessmentResult(
                riskScore = 34,
                riskLevel = "Low Risk",
                date = "Oct 24, 2023",
                symptomCount = _uiState.value.selectedSymptoms.size,
                aiExplanation = "Based on your reported symptoms of headache and mild fatigue, your overall risk level is low. These symptoms are commonly associated with tension or seasonal factors.",
                conditions = listOf(
                    AssessmentCondition("c1", "Tension Headache", "Mild", 78, "🤕"),
                    AssessmentCondition("c2", "Viral Upper Respiratory", "Mild-Moderate", 62, "😷"),
                    AssessmentCondition("c3", "Stress & Anxiety", "Mild", 55, "🧠")
                ),
                recommendations = listOf(
                    Recommendation("r1", "See a Doctor", "🩺"),
                    Recommendation("r2", "Rest", "🛌"),
                    Recommendation("r3", "Stay Hydrated", "💧"),
                    Recommendation("r4", "OTC Medication", "💊")
                ),
                specialist = RecommendedSpecialist(
                    title = "General Practitioner / Internist",
                    description = "Book within 3–5 days if no improvement"
                )
            )

            _uiState.update { it.copy(
                isAnalyzing = false,
                result = dummyResult
            ) }
        }
    }
}
