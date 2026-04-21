package com.example.rahul.symptoscan.data.repository

import com.example.rahul.symptoscan.data.local.SymptomDao
import com.example.rahul.symptoscan.data.local.SymptomEntity
import com.example.rahul.symptoscan.data.remote.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class SymptomRepository(
    private val symptomDao: SymptomDao,
    private val apiService: GeminiApiService,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val apiKey = "AIzaSyBvsgR-KnwDxTLWY4aF5OdQXnktixMZ8Uc"

    fun getAllHistory(): Flow<List<SymptomEntity>> =
        symptomDao.getAllSymptoms()

    // ✅ SAFE API CALL WITH RETRY
    private suspend fun safeApiCall(request: GeminiRequest): GeminiResponse {
        repeat(3) { attempt ->
            try {
                return apiService.analyzeSymptoms(apiKey, request)
            } catch (e: Exception) {
                if (attempt == 2) throw e
                delay(2000)
            }
        }
        throw Exception("API Failed")
    }

    // ✅ ANALYZE
    suspend fun analyzeSymptoms(symptoms: String): SymptomAnalysis {

        val prompt = """
            You are a medical assistant.

            Symptoms: $symptoms

            Provide:
            - Conditions
            - Risk Level (Low/Medium/High)
            - Suggested Action

            Respond ONLY in JSON:
            {
              "conditions": ["..."],
              "riskLevel": "...",
              "suggestedAction": "..."
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                RequestContent(
                    parts = listOf(
                        RequestPart(text = prompt)
                    )
                )
            )
        )

        return try {

            val response = safeApiCall(request)

            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text ?: "No response"

            val cleanJson = text
                .replace("```json", "")
                .replace("```", "")
                .trim()

            try {
                com.google.gson.Gson()
                    .fromJson(cleanJson, SymptomAnalysis::class.java)
            } catch (e: Exception) {
                SymptomAnalysis(
                    conditions = listOf("Possible issue"),
                    riskLevel = "Medium",
                    suggestedAction = cleanJson
                )
            }

        } catch (e: Exception) {
            SymptomAnalysis(
                conditions = listOf("Error"),
                riskLevel = "Unknown",
                suggestedAction = "Server busy, try again"
            )
        }
    }

    // ✅ SAVE
    suspend fun saveSymptomResult(entity: SymptomEntity) {
        symptomDao.insertSymptom(entity)

        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users")
                .document(userId)
                .collection("history")
                .add(entity)
                .await()
        }
    }

    suspend fun deleteSymptom(entity: SymptomEntity) {
        symptomDao.deleteSymptom(entity)
    }

    // ✅ CHAT
    suspend fun getChatResponse(message: String): String {

        return try {

            val request = GeminiRequest(
                contents = listOf(
                    RequestContent(
                        parts = listOf(
                            RequestPart(text = message)
                        )
                    )
                )
            )

            val response = safeApiCall(request)

            response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text ?: "No response"

        } catch (e: Exception) {
            "Server busy, try again"
        }
    }
}