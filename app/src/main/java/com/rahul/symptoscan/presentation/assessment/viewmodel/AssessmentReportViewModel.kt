package com.rahul.symptoscan.presentation.assessment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AssessmentRepository
import com.rahul.symptoscan.presentation.assessment.state.AssessmentReportUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssessmentReportViewModel(
    private val repository: AssessmentRepository = Injection.assessmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentReportUiState())
    val uiState: StateFlow<AssessmentReportUiState> = _uiState.asStateFlow()

    fun loadReport(assessmentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getAssessmentReport(assessmentId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load report") }
                }
                .collect { report ->
                    _uiState.update { it.copy(isLoading = false, report = report) }
                }
        }
    }
}
