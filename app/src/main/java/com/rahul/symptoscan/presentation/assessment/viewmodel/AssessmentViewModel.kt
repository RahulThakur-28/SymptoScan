package com.rahul.symptoscan.presentation.assessment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.remote.model.DbAssessmentSymptom
import com.rahul.symptoscan.domain.model.Symptom
import com.rahul.symptoscan.presentation.assessment.state.AssessmentUiState
import com.rahul.symptoscan.presentation.assessment.state.SymptomDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssessmentViewModel(
    private val repository: com.rahul.symptoscan.data.repository.AssessmentRepository = Injection.assessmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState: StateFlow<AssessmentUiState> = _uiState.asStateFlow()

    init {
        loadSymptoms()
    }

    private fun loadSymptoms() {
        val symptoms = listOf(
            Symptom("1", "Fever", "🤒", "General"),
            Symptom("2", "Cough", "💨", "Respiratory"),
            Symptom("3", "Fatigue", "😴", "General"),
            Symptom("4", "Headache", "🤕", "Neurological"),
            Symptom("5", "Sore Throat", "👄", "Respiratory"),
            Symptom("6", "Nausea", "🤢", "Digestive"),
            Symptom("7", "Dizziness", "😵‍💫", "Neurological"),
            Symptom("8", "Muscle Pain", "💪", "General")
        )
        _uiState.update { it.copy(symptoms = symptoms, recentSymptoms = symptoms.take(4)) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSymptomToggle(symptomId: String) {
        _uiState.update { state ->
            val isSelected = state.selectedSymptoms.any { it.id == symptomId }
            val newList = if (isSelected) {
                state.selectedSymptoms.filter { it.id != symptomId }
            } else {
                val symptom = state.symptoms.find { it.id == symptomId } 
                    ?: state.selectedSymptoms.find { it.id == symptomId }
                    ?: return@update state
                state.selectedSymptoms + symptom
            }
            state.copy(selectedSymptoms = newList)
        }
    }

    fun addCustomSymptom(name: String) {
        if (name.isBlank()) return
        if (_uiState.value.selectedSymptoms.any { it.name.equals(name, ignoreCase = true) }) return
        
        val newSymptom = Symptom(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            icon = "✨",
            category = "Custom",
            isSelected = true
        )
        _uiState.update { state ->
            state.copy(
                selectedSymptoms = state.selectedSymptoms + newSymptom,
                searchQuery = ""
            )
        }
    }

    fun onSymptomDetailsChange(symptomId: String, details: SymptomDetails) {
        _uiState.update { state ->
            val newDetails = state.symptomDetails.toMutableMap()
            newDetails[symptomId] = details
            state.copy(symptomDetails = newDetails)
        }
    }

    fun onTemperatureChange(temp: Double) {
        _uiState.update { it.copy(bodyTemperature = temp) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(additionalNotes = notes) }
    }

    fun onImageSelected(uri: android.net.Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun removeImage() {
        _uiState.update { it.copy(selectedImageUri = null) }
    }

    private fun fahrenheitToCelsius(f: Double): Double {
        return (f - 32) * 5 / 9
    }

    fun startAssessment(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val state = _uiState.value
            val currentId = state.assessmentId
            
            // Convert to Celsius for backend storage
            val tempCelsius = fahrenheitToCelsius(state.bodyTemperature)

            var uploadedImagePath: String? = null
            if (state.selectedImageUri != null) {
                _uiState.update { it.copy(isImageUploading = true) }
                val bytes = com.rahul.symptoscan.core.utils.ImageUtils.uriToByteArray(
                    Injection.applicationContext,
                    state.selectedImageUri
                )
                if (bytes != null) {
                    val fileName = "assessment_${System.currentTimeMillis()}.jpg"
                    repository.uploadAssessmentImage(bytes, fileName).onSuccess { path ->
                        uploadedImagePath = path
                    }.onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, isImageUploading = false, error = "Failed to upload image: ${e.message}") }
                        return@launch
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isImageUploading = false, error = "Failed to process image") }
                    return@launch
                }
                _uiState.update { it.copy(isImageUploading = false) }
            }
            
            if (currentId != null) {
                // Update context for existing assessment
                repository.updateAssessmentContext(currentId, tempCelsius, state.additionalNotes, uploadedImagePath)
                saveSymptoms(onComplete)
                return@launch
            }

            repository.createAssessment(tempCelsius, state.additionalNotes, uploadedImagePath).onSuccess { id ->
                _uiState.update { it.copy(assessmentId = id) }
                saveSymptoms(onComplete)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to start assessment") }
            }
        }
    }

    private suspend fun saveSymptoms(onComplete: () -> Unit) {
        val state = _uiState.value
        val assessmentId = state.assessmentId!!
        val dbSymptoms = state.selectedSymptoms.map { symptom ->
            val details = state.symptomDetails[symptom.id] ?: SymptomDetails()
            DbAssessmentSymptom(
                assessmentId = assessmentId,
                symptomName = symptom.name,
                isCustom = symptom.category == "Custom",
                severity = details.severity,
                painLevel = details.painLevel,
                duration = details.duration,
                frequency = details.frequency,
                onset = details.onset
            )
        }
        
        repository.saveSymptoms(assessmentId, dbSymptoms).onSuccess {
            generateQuestions(onComplete)
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to save symptoms") }
        }
    }

    private suspend fun generateQuestions(onComplete: () -> Unit) {
        repository.generateQuestions(_uiState.value.assessmentId!!).onSuccess { questions ->
            _uiState.update { it.copy(isLoading = false, questions = questions, currentQuestionIndex = 0) }
            onComplete()
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to generate questions") }
        }
    }

    fun updateAnswer(index: Int, answer: String) {
        _uiState.update { state ->
            val newQuestions = state.questions.toMutableList()
            newQuestions[index] = newQuestions[index].copy(answer = answer)
            state.copy(questions = newQuestions)
        }
    }

    fun nextQuestion() {
        if (_uiState.value.currentQuestionIndex < _uiState.value.questions.size - 1) {
            _uiState.update { it.copy(currentQuestionIndex = it.currentQuestionIndex + 1) }
        }
    }

    fun previousQuestion() {
        if (_uiState.value.currentQuestionIndex > 0) {
            _uiState.update { it.copy(currentQuestionIndex = it.currentQuestionIndex - 1) }
        }
    }

    fun submitAnswers(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.saveAnswers(_uiState.value.questions).onSuccess {
                generateFinalResult(onComplete)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to save answers") }
            }
        }
    }

    private suspend fun generateFinalResult(onComplete: () -> Unit) {
        repository.generateResult(_uiState.value.assessmentId!!).onSuccess { result ->
            _uiState.update { it.copy(isLoading = false, result = result) }
            onComplete()
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to generate result") }
        }
    }
}
