package com.rahul.symptoscan.presentation.assessment.state

import com.rahul.symptoscan.domain.model.FullAssessmentReport

data class AssessmentReportUiState(
    val isLoading: Boolean = false,
    val report: FullAssessmentReport? = null,
    val error: String? = null
)
