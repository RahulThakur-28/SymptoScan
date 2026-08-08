package com.rahul.symptoscan.presentation.history.state

import com.rahul.symptoscan.domain.model.AssessmentStatus
import com.rahul.symptoscan.domain.model.AssessmentSummary

data class HistoryUiState(
    val assessments: List<AssessmentSummary> = emptyList(),
    val filteredAssessments: List<AssessmentSummary> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: AssessmentStatus? = null, // null for 'All'
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Statistics derived from data
    val totalCount: Int = 0,
    val lowCount: Int = 0,
    val moderateCount: Int = 0,
    val highCount: Int = 0
)
