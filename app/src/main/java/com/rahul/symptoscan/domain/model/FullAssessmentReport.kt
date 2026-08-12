package com.rahul.symptoscan.domain.model

import com.rahul.symptoscan.data.remote.model.DbAssessment
import com.rahul.symptoscan.data.remote.model.DbAssessmentQuestion
import com.rahul.symptoscan.data.remote.model.DbAssessmentResult
import com.rahul.symptoscan.data.remote.model.DbAssessmentSymptom

data class FullAssessmentReport(
    val assessment: DbAssessment,
    val result: DbAssessmentResult,
    val symptoms: List<DbAssessmentSymptom>,
    val questions: List<DbAssessmentQuestion>
)
