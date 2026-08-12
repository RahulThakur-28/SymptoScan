package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.data.remote.SupabaseClient
import com.rahul.symptoscan.data.remote.model.*
import com.rahul.symptoscan.domain.model.*
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AssessmentRepository {

    private val postgrest = SupabaseClient.database
    private val auth = SupabaseClient.auth
    private val functions = SupabaseClient.supabase.functions

    suspend fun createAssessment(temperature: Double, notes: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
            val assessment = DbAssessment(
                userId = userId,
                bodyTemperature = temperature,
                additionalNotes = notes
            )
            
            android.util.Log.d("AssessmentRepository", "Creating assessment for user: $userId")
            
            val result = postgrest.from("assessments").insert(assessment) {
                select()
            }.decodeSingle<DbAssessment>()
            
            val id = result.id ?: throw IllegalStateException("Failed to get assessment ID")
            android.util.Log.d("AssessmentRepository", "Assessment created successfully. assessmentId = $id")
            id
        }
    }

    suspend fun updateAssessmentContext(assessmentId: String, temperature: Double, notes: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            android.util.Log.d("AssessmentRepository", "Updating context for assessmentId = $assessmentId")
            postgrest.from("assessments").update(buildJsonObject {
                put("body_temperature", temperature)
                put("additional_notes", notes)
            }) {
                filter { eq("id", assessmentId) }
            }
            Unit
        }
    }

    suspend fun saveSymptoms(assessmentId: String, symptoms: List<DbAssessmentSymptom>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            android.util.Log.d("AssessmentRepository", "Saving ${symptoms.size} symptoms for assessmentId = $assessmentId")
            // Clear existing symptoms to prevent duplicates on retry
            postgrest.from("assessment_symptoms").delete {
                filter { eq("assessment_id", assessmentId) }
            }
            // Insert new symptoms
            postgrest.from("assessment_symptoms").insert(symptoms)
            android.util.Log.d("AssessmentRepository", "Symptoms saved successfully")
            Unit
        }
    }

    suspend fun generateQuestions(assessmentId: String): Result<List<DbAssessmentQuestion>> = withContext(Dispatchers.IO) {
        runCatching {
            android.util.Log.d("AssessmentRepository", "Generating questions for assessmentId = $assessmentId")
            functions.invoke("generate-assessment-questions", body = buildJsonObject {
                put("assessmentId", assessmentId)
            })
            
            val questions = postgrest.from("assessment_questions")
                .select() {
                    filter { eq("assessment_id", assessmentId) }
                    order("question_order", Order.ASCENDING)
                }
                .decodeList<DbAssessmentQuestion>()
            
            android.util.Log.d("AssessmentRepository", "Received ${questions.size} questions from Edge Function/DB")
            questions
        }
    }

    suspend fun saveAnswers(questions: List<DbAssessmentQuestion>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            questions.forEach { question ->
                postgrest.from("assessment_questions").update(buildJsonObject {
                    put("answer", question.answer)
                }) {
                    filter { eq("id", question.id!!) }
                }
            }
            Unit
        }
    }

    suspend fun generateResult(assessmentId: String): Result<DbAssessmentResult> = withContext(Dispatchers.IO) {
        runCatching {
            functions.invoke("generate-assessment-result", body = buildJsonObject {
                put("assessmentId", assessmentId)
            })
            
            postgrest.from("assessment_results")
                .select() {
                    filter { eq("assessment_id", assessmentId) }
                }
                .decodeSingle<DbAssessmentResult>()
        }
    }

    fun getAssessmentHistory(): Flow<List<AssessmentSummary>> = flow {
        val userId = auth.currentUserOrNull()?.id ?: return@flow
        try {
            val assessments = postgrest.from("assessments")
                .select(columns = Columns.raw("id, status, created_at, assessment_results(summary, urgency_level)")) {
                    filter { eq("user_id", userId); eq("status", "completed") }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<DbAssessmentWithResult>()
            
            emit(assessments.map { 
                AssessmentSummary(
                    id = it.id,
                    title = it.result?.summary?.take(30)?.plus("...") ?: "Health Assessment",
                    time = it.createdAt ?: "",
                    status = mapUrgency(it.result?.urgencyLevel),
                    score = 0,
                    symptoms = emptyList()
                )
            })
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    private fun mapUrgency(urgency: String?): AssessmentStatus {
        return when (urgency?.lowercase()) {
            "emergency" -> AssessmentStatus.High
            "urgent" -> AssessmentStatus.High
            "routine" -> AssessmentStatus.Low
            "self_care" -> AssessmentStatus.Low
            else -> AssessmentStatus.Moderate
        }
    }

    fun getAssessmentReport(id: String): Flow<DbAssessmentResult> = flow {
        val result = postgrest.from("assessment_results")
            .select() {
                filter { eq("assessment_id", id) }
            }
            .decodeSingle<DbAssessmentResult>()
        emit(result)
    }

    suspend fun getAssessmentCount(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val response = postgrest.from("assessments")
                .select(columns = Columns.raw("id")) {
                    filter { eq("user_id", userId); eq("status", "completed") }
                }
            response.decodeList<DbAssessment>().size
        } catch (e: Exception) {
            0
        }
    }
}
