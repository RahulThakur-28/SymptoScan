package com.rahul.symptoscan.domain.model

data class AssessmentReport(
    val id: String,
    val assessmentTitle: String,
    val riskScore: Int,
    val riskLevel: String,
    val confidence: Int,
    val patientInfo: PatientInfo,
    val assessmentDate: String,
    val duration: String,
    val symptomsAssessedCount: Int,
    val reportedSymptoms: List<ReportedSymptom>,
    val clinicalInsights: String
)

data class PatientInfo(
    val name: String,
    val dob: String,
    val age: Int,
    val bloodGroup: String?,
    val gender: String
)

data class ReportedSymptom(
    val id: String,
    val name: String,
    val icon: String,
    val duration: String,
    val painLevel: Int,
    val severity: String
)
