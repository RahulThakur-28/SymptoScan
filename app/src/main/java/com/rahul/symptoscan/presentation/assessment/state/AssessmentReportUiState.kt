package com.rahul.symptoscan.presentation.assessment.state

import com.rahul.symptoscan.domain.model.AssessmentReport

data class AssessmentReportUiState(
    val isLoading: Boolean = false,
    val report: AssessmentReport? = null,
    val error: String? = null
)
