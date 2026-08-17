package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.data.remote.SupabaseClient
import com.rahul.symptoscan.data.remote.model.*
import com.rahul.symptoscan.domain.model.*
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.call.body
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
    private val storage = SupabaseClient.storage

    suspend fun uploadAssessmentImage(bytes: ByteArray, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
            val path = "$userId/$fileName"
            android.util.Log.d("AssessmentRepository", "[Assessment] Uploading image: $path")
            storage.from("assessment-images").upload(path, bytes) {
                upsert = true
            }
            android.util.Log.d("AssessmentRepository", "[Assessment] Image upload complete")
            path
        }
    }

    suspend fun createAssessment(temperature: Double, notes: String, imageUrl: String? = null): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
            val assessment = DbAssessment(
                userId = userId,
                bodyTemperature = temperature,
                additionalNotes = notes,
                imageUrl = imageUrl
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

    suspend fun updateAssessmentContext(assessmentId: String, temperature: Double, notes: String, imageUrl: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            android.util.Log.d("AssessmentRepository", "Updating context for assessmentId = $assessmentId")
            postgrest.from("assessments").update(buildJsonObject {
                put("body_temperature", temperature)
                put("additional_notes", notes)
                imageUrl?.let { put("image_url", it) }
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

    suspend fun generateQuestions(
        assessmentId: String,
        context: AiAssessmentContext
    ): Result<List<DbAssessmentQuestion>> = withContext(Dispatchers.IO) {
        runCatching {
            val startTime = System.currentTimeMillis()
            android.util.Log.d("AssessmentRepository", "[AI][Questions] Started for id: $assessmentId")
            
            // Check if questions already exist
            val existing = postgrest.from("assessment_questions")
                .select() {
                    filter { eq("assessment_id", assessmentId) }
                }.decodeList<DbAssessmentQuestion>()
            
            if (existing.isNotEmpty()) {
                android.util.Log.d("AssessmentRepository", "[Questions] Existing questions found, skipping generation")
                return@runCatching existing.sortedBy { it.questionOrder }
            }

            android.util.Log.d("AssessmentRepository", "[AI][Questions] Gemini request started")
            functions.invoke(
                function = "generate-assessment-questions",
                body = buildJsonObject {
                    put("assessmentId", assessmentId)
                    put("context", kotlinx.serialization.json.Json.encodeToJsonElement(AiAssessmentContext.serializer(), context))
                }
            )
            val requestEndTime = System.currentTimeMillis()
            android.util.Log.d("AssessmentRepository", "[AI][Questions] Request completed: ${requestEndTime - startTime} ms")
            
            val questions = postgrest.from("assessment_questions")
                .select() {
                    filter { eq("assessment_id", assessmentId) }
                    order("question_order", Order.ASCENDING)
                }
                .decodeList<DbAssessmentQuestion>()
            
            val totalTime = System.currentTimeMillis() - startTime
            android.util.Log.d("AssessmentRepository", "[AI][Questions] Received ${questions.size} questions. Total: $totalTime ms")
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

    suspend fun generateResult(
        assessmentId: String,
        context: CompleteAiAssessmentContext
    ): Result<DbAssessmentResult> = withContext(Dispatchers.IO) {
        runCatching {
            val startTime = System.currentTimeMillis()
            android.util.Log.d("AssessmentRepository", "[AI][Result] Started for id: $assessmentId")
            
            // 1. Invoke Edge Function and get the FULL result back
            val response = functions.invoke(
                function = "generate-assessment-result",
                body = buildJsonObject {
                    put("assessmentId", assessmentId)
                    put("completeContext", kotlinx.serialization.json.Json.encodeToJsonElement(CompleteAiAssessmentContext.serializer(), context))
                }
            )
            
            val requestEndTime = System.currentTimeMillis()
            android.util.Log.d("AssessmentRepository", "[AI][Result] Request completed: ${requestEndTime - startTime} ms")

            val result = response.body<DbAssessmentResult>()
            
            val dbSaveStartTime = System.currentTimeMillis()
            // The result is already saved by the Edge Function in the DB, 
            // but we might want to ensure consistency or update status.
            postgrest.from("assessments").update(buildJsonObject {
                put("status", "completed")
                put("completed_at", com.rahul.symptoscan.core.utils.DateUtils.getCurrentIsoTimestamp())
            }) {
                filter { eq("id", assessmentId) }
            }

            val totalTime = System.currentTimeMillis() - startTime
            android.util.Log.d("AssessmentRepository", "[AI][Result] Database update: ${System.currentTimeMillis() - dbSaveStartTime} ms. Total: $totalTime ms")
            result
        }
    }

    fun getAssessmentHistory(): Flow<List<AssessmentSummary>> = flow {
        val userId = auth.currentUserOrNull()?.id ?: return@flow
        val historyItems = try {
            android.util.Log.d("AssessmentRepository", "[History] Fetching history for userId: $userId")
            
            // We fetch all assessments for the user and then filter for those that have a generated result.
            // This is more robust than relying on a 'status' flag that might have failed to update.
            val assessments = postgrest.from("assessments")
                .select(columns = Columns.raw("id, image_url, status, created_at, assessment_results(summary, urgency_level, risk_score, assessment_id), assessment_symptoms(symptom_name)")) {
                    filter { 
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<DbAssessmentWithResult>()
            
            android.util.Log.d("AssessmentRepository", "[History] Total assessments found: ${assessments.size}")

            assessments
                .filter { it.result != null } // Only show assessments with AI results
                .map {
                    val res = it.result!!
                    AssessmentSummary(
                        id = it.id,
                        title = res.summary?.take(50)?.plus("...") ?: "Health Assessment",
                        time = com.rahul.symptoscan.core.utils.DateUtils.formatIsoToReadable(it.createdAt),
                        status = mapUrgency(res.urgencyLevel),
                        score = res.riskScore,
                        symptoms = it.symptoms.map { s -> s.symptomName },
                        hasImage = !it.imageUrl.isNullOrBlank()
                    )
                }
        } catch (e: Exception) {
            android.util.Log.e("AssessmentRepository", "[History] Error fetching history: ${e.message}", e)
            emptyList<AssessmentSummary>()
        }
        
        android.util.Log.d("AssessmentRepository", "[History] Emitting ${historyItems.size} items to history")
        emit(historyItems)
    }

    fun calculateHealthScore(history: List<AssessmentSummary>): Int {
        if (history.isEmpty()) return 100
        
        // Take latest 5 completed assessments with valid scores
        val validScores = history.map { it.score }.filterNotNull().take(5)
        if (validScores.isEmpty()) return 100
        
        val averageRisk = validScores.average()
        
        return (100 - averageRisk.toInt()).coerceIn(0, 100)
    }

    fun getLatestCompletedAssessment(): Flow<DbAssessmentWithResult?> = flow {
        val userId = auth.currentUserOrNull()?.id ?: return@flow
        val assessment = try {
            postgrest.from("assessments")
                .select(columns = Columns.raw("id, image_url, status, created_at, assessment_results(risk_score)")) {
                    filter {
                        eq("user_id", userId)
                        eq("status", "completed")
                    }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<DbAssessmentWithResult>()
        } catch (e: Exception) {
            null
        }
        emit(assessment)
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

    suspend fun deleteAssessment(assessmentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            android.util.Log.d("AssessmentRepository", "[Delete] Deleting assessment: $assessmentId")
            
            // Delete related records manually if cascade is not enabled in DB
            postgrest.from("assessment_symptoms").delete {
                filter { eq("assessment_id", assessmentId) }
            }
            postgrest.from("assessment_questions").delete {
                filter { eq("assessment_id", assessmentId) }
            }
            postgrest.from("assessment_results").delete {
                filter { eq("assessment_id", assessmentId) }
            }
            
            // Delete the main assessment record
            postgrest.from("assessments").delete {
                filter { eq("id", assessmentId) }
            }
            
            android.util.Log.d("AssessmentRepository", "[Delete] Assessment deleted successfully")
            Unit
        }
    }
}
