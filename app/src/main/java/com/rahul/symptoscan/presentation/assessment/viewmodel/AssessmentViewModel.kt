package com.rahul.symptoscan.presentation.assessment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.remote.model.DbAssessmentSymptom
import com.rahul.symptoscan.data.repository.AssessmentRepository
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.HealthProfileRepository
import com.rahul.symptoscan.domain.model.*
import com.rahul.symptoscan.presentation.assessment.state.AssessmentUiState
import com.rahul.symptoscan.presentation.assessment.state.SymptomDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssessmentViewModel(
    private val repository: AssessmentRepository = Injection.assessmentRepository,
    private val authRepository: AuthRepository = Injection.authRepository,
    private val healthProfileRepository: HealthProfileRepository = Injection.healthProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState: StateFlow<AssessmentUiState> = _uiState.asStateFlow()

    private var sessionContext: AiAssessmentContext? = null

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

    private suspend fun buildAiAssessmentContext(): AiAssessmentContext = coroutineScope {
        val userId = authRepository.getCurrentUser()?.id ?: ""
        val state = _uiState.value
        
        val healthProfileDeferred = async { 
            healthProfileRepository.getHealthProfile(userId).firstOrNull()
        }
        
        val healthProfile = healthProfileDeferred.await()
        
        val age = healthProfile?.dateOfBirth?.let { dob ->
            try {
                val birthDate = java.time.LocalDate.parse(dob)
                java.time.Period.between(birthDate, java.time.LocalDate.now()).years
            } catch (e: Exception) {
                null
            }
        }

        val aiHealthContext = AiHealthProfileContext(
            age = age,
            biologicalSex = healthProfile?.biologicalSex,
            bloodGroup = healthProfile?.bloodGroup,
            heightCm = healthProfile?.heightCm,
            weightKg = healthProfile?.weightKg,
            allergies = healthProfile?.allergies,
            existingConditions = healthProfile?.medicalConditions,
            currentMedicines = healthProfile?.medications
        )

        AiAssessmentContext(
            symptoms = state.selectedSymptoms.map { it.name },
            description = state.additionalNotes.ifBlank { null },
            bodyTemperature = state.bodyTemperature,
            hasImage = state.selectedImageUri != null,
            healthProfile = aiHealthContext
        )
    }

    fun startAssessment(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val startTime = System.currentTimeMillis()
            android.util.Log.d("AssessmentViewModel", "[Assessment] Session Started")

            val state = _uiState.value
            val currentId = state.assessmentId
            
            // 1. Concurrent Fetch: Prepare AI Context & Image if exists
            val contextDeferred = async { buildAiAssessmentContext() }
            
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
                        _uiState.update { it.copy(isLoading = false, isImageUploading = false, error = com.rahul.symptoscan.core.utils.ErrorUtils.getUserFriendlyMessage(e)) }
                        return@launch
                    }
                }
                _uiState.update { it.copy(isImageUploading = false) }
            }

            val context = contextDeferred.await()
            sessionContext = context
            android.util.Log.d("AssessmentViewModel", "[Assessment] Context ready: ${System.currentTimeMillis() - startTime} ms")

            // Convert to Celsius for backend storage
            val tempCelsius = fahrenheitToCelsius(state.bodyTemperature)

            // 2. Database Sync
            if (currentId != null) {
                repository.updateAssessmentContext(currentId, tempCelsius, state.additionalNotes, uploadedImagePath)
                saveSymptoms(onComplete)
                return@launch
            }

            repository.createAssessment(tempCelsius, state.additionalNotes, uploadedImagePath).onSuccess { id ->
                _uiState.update { it.copy(assessmentId = id) }
                saveSymptoms(onComplete)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = com.rahul.symptoscan.core.utils.ErrorUtils.getUserFriendlyMessage(e)) }
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
            _uiState.update { it.copy(isLoading = false, error = com.rahul.symptoscan.core.utils.ErrorUtils.getUserFriendlyMessage(e)) }
        }
    }

    private suspend fun generateQuestions(onComplete: () -> Unit) {
        val assessmentId = _uiState.value.assessmentId!!
        val context = sessionContext ?: buildAiAssessmentContext()

        repository.generateQuestions(assessmentId, context).onSuccess { questions ->
            _uiState.update { it.copy(isLoading = false, questions = questions, currentQuestionIndex = 0) }
            onComplete()
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = com.rahul.symptoscan.core.utils.ErrorUtils.getUserFriendlyMessage(e)) }
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
                _uiState.update { it.copy(isLoading = false, error = com.rahul.symptoscan.core.utils.ErrorUtils.getUserFriendlyMessage(e)) }
            }
        }
    }

    private suspend fun generateFinalResult(onComplete: () -> Unit) {
        val assessmentId = _uiState.value.assessmentId!!
        val state = _uiState.value
        
        val context = sessionContext ?: buildAiAssessmentContext()
        val completeContext = CompleteAiAssessmentContext(
            initialContext = context,
            followUpAnswers = state.questions.map { 
                AiQuestionAnswer(it.question, it.answer ?: "Not answered") 
            }
        )
        
        val startTime = System.currentTimeMillis()
        android.util.Log.d("AssessmentViewModel", "[AI][Result] Requesting final result for: $assessmentId")
        
        repository.generateResult(assessmentId, completeContext).onSuccess { result ->
            val totalTime = System.currentTimeMillis() - startTime
            android.util.Log.d("AssessmentViewModel", "[AI][Result] Successfully received assessment result. riskScore = ${result.riskScore}. Total: $totalTime ms")
            _uiState.update { it.copy(isLoading = false, result = result) }
            onComplete()
        }.onFailure { e ->
            val totalTime = System.currentTimeMillis() - startTime
            if (com.rahul.symptoscan.BuildConfig.DEBUG) {
                android.util.Log.e("AssessmentViewModel", "[AI][Result] Failed to generate result after $totalTime ms: ${e.message}")
            }
            _uiState.update { it.copy(isLoading = false, error = com.rahul.symptoscan.core.utils.ErrorUtils.getUserFriendlyMessage(e)) }
        }
    }

    fun loadAssessmentResult(id: String) {
        if (_uiState.value.assessmentId == id && _uiState.value.result != null) {
            android.util.Log.d("AssessmentViewModel", "[Report] Result already loaded for id: $id")
            return
        }
        
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            android.util.Log.d("AssessmentViewModel", "[Report] Opening assessment: $id")
            android.util.Log.d("AssessmentViewModel", "[Report] Fetch started: $id")
            _uiState.update { it.copy(isLoading = true, assessmentId = id, result = null, error = null) }
            repository.getAssessmentReport(id)
                .catch { e ->
                    if (com.rahul.symptoscan.BuildConfig.DEBUG) {
                        android.util.Log.e("AssessmentViewModel", "[Report] Fetch failed: ${e.message}")
                    }
                    _uiState.update { it.copy(isLoading = false, error = com.rahul.symptoscan.core.utils.ErrorUtils.getUserFriendlyMessage(e)) }
                }
                .collect { result ->
                    val duration = System.currentTimeMillis() - startTime
                    android.util.Log.d("AssessmentViewModel", "[Report] Fetch completed: $id")
                    android.util.Log.d("AssessmentViewModel", "[Report] Fetch duration: $duration ms")
                    _uiState.update { it.copy(isLoading = false, result = result) }
                }
        }
    }
}
