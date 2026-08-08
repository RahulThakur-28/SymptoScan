package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository to manage health assessment data.
 */
class AssessmentRepository {

    /**
     * Fetches assessment history from Supabase/DB.
     */
    fun getAssessmentHistory(): Flow<List<AssessmentSummary>> = flow {
        // ... (existing history code)
        delay(1000)
        emit(
            listOf(
                AssessmentSummary(
                    id = "1",
                    title = "Headache & Fatigue",
                    time = "Today, 11:30 AM",
                    status = AssessmentStatus.Low,
                    score = 28,
                    symptoms = listOf("Headache", "Fatigue")
                ),
                AssessmentSummary(
                    id = "2",
                    title = "Chest Discomfort",
                    time = "Aug 2, 2:15 PM",
                    status = AssessmentStatus.Moderate,
                    score = 54,
                    symptoms = listOf("Chest Pain", "Shortness of Breath")
                ),
                AssessmentSummary(
                    id = "3",
                    title = "Seasonal Allergies",
                    time = "1 week ago",
                    status = AssessmentStatus.Low,
                    score = 22,
                    symptoms = listOf("Cough", "Sore Throat", "Sneezing")
                ),
                AssessmentSummary(
                    id = "4",
                    title = "Severe Migraine",
                    time = "2 weeks ago",
                    status = AssessmentStatus.High,
                    score = 85,
                    symptoms = listOf("Headache", "Nausea", "Dizziness")
                ),
                AssessmentSummary(
                    id = "5",
                    title = "Minor Fever",
                    time = "Aug 10, 9:00 AM",
                    status = AssessmentStatus.Low,
                    score = 15,
                    symptoms = listOf("Fever")
                ),
                AssessmentSummary(
                    id = "6",
                    title = "Back Strain",
                    time = "Aug 5, 4:45 PM",
                    status = AssessmentStatus.Moderate,
                    score = 42,
                    symptoms = listOf("Back Pain")
                )
            )
        )
    }

    /**
     * Fetches a detailed assessment report by ID.
     */
    fun getAssessmentReport(id: String): Flow<AssessmentReport> = flow {
        delay(1000)
        // Simulating fetching a specific report
        emit(
            AssessmentReport(
                id = id,
                assessmentTitle = "Headache & Fatigue",
                riskScore = 34,
                riskLevel = "Low Risk",
                confidence = 78,
                patientInfo = PatientInfo(
                    name = "Sarah Johnson",
                    dob = "March 15, 1992",
                    age = 32,
                    bloodGroup = "O+",
                    gender = "Female"
                ),
                assessmentDate = "Sat, Aug 8, 2026",
                duration = "~12 minutes",
                symptomsAssessedCount = 3,
                reportedSymptoms = listOf(
                    ReportedSymptom("s1", "Headache", "🤕", "2–3 days", 5, "Moderate"),
                    ReportedSymptom("s2", "Fatigue", "😴", "2–3 days", 3, "Mild"),
                    ReportedSymptom("s3", "Fever", "🤒", "Today", 2, "Mild")
                ),
                clinicalInsights = "The presented symptom cluster — headache with associated fatigue and mild low-grade fever — demonstrates a pattern consistent with tension-type headache with possible early viral illness. The gradual onset (2–3 days), intermittent pattern, and mild severity (5/10) suggest a self-limiting condition without immediate red flags.\n\nKey reassuring factors include the absence of sudden severe onset (\"thunderclap\"), neurological symptoms, or constitutional symptoms such as significant weight loss or night sweats."
            )
        )
    }
}
