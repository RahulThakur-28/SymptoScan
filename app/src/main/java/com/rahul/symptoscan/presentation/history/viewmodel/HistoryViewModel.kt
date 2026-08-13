package com.rahul.symptoscan.presentation.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AssessmentRepository
import com.rahul.symptoscan.domain.model.AssessmentStatus
import com.rahul.symptoscan.domain.model.AssessmentSummary
import com.rahul.symptoscan.presentation.history.state.HistoryUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: AssessmentRepository = Injection.assessmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun onRefresh() {
        loadHistory(isRefresh = true)
    }

    fun loadHistory(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            
            repository.getAssessmentHistory()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load history") }
                }
                .collect { assessments ->
                    _uiState.update { state ->
                        state.copy(
                            assessments = assessments,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                    applyFilters()
                    calculateStats()
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onFilterSelected(status: AssessmentStatus?) {
        _uiState.update { it.copy(selectedFilter = status) }
        applyFilters()
    }

    private fun applyFilters() {
        _uiState.update { state ->
            val filtered = state.assessments.filter { assessment ->
                val matchesSearch = state.searchQuery.isEmpty() ||
                    assessment.title.contains(state.searchQuery, ignoreCase = true) ||
                    assessment.symptoms.any { it.contains(state.searchQuery, ignoreCase = true) }

                val matchesFilter = state.selectedFilter == null || assessment.status == state.selectedFilter

                matchesSearch && matchesFilter
            }
            state.copy(filteredAssessments = filtered)
        }
    }

    private fun calculateStats() {
        _uiState.update { state ->
            state.copy(
                totalCount = state.assessments.size,
                lowCount = state.assessments.count { it.status == AssessmentStatus.Low },
                moderateCount = state.assessments.count { it.status == AssessmentStatus.Moderate },
                highCount = state.assessments.count { it.status == AssessmentStatus.High }
            )
        }
    }
}
