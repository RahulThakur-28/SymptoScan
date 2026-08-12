package com.rahul.symptoscan.presentation.assessment.state

import com.rahul.symptoscan.data.remote.model.DbAssessmentResult

data class AssessmentReportUiState(
    val isLoading: Boolean = false,
    val report: DbAssessmentResult? = null,
    val error: String? = null
)
