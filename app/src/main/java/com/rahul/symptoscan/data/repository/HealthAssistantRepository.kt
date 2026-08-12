package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.data.remote.SupabaseClient
import com.rahul.symptoscan.data.remote.model.*
import com.rahul.symptoscan.domain.model.*
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HealthAssistantRepository {

    private val postgrest = SupabaseClient.database
    private val auth = SupabaseClient.auth
    private val functions = SupabaseClient.supabase.functions

    private fun DbHealthConversation.asDomain() = HealthConversation(
        id = id ?: "",
        userId = userId,
        title = title,
        language = language,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun DbHealthMessage.asDomain() = HealthMessage(
        id = id ?: "",
        conversationId = conversationId,
        userId = userId,
        role = role,
        content = content,
        createdAt = createdAt
    )

    suspend fun createConversation(
        title: String?,
        language: String
    ): Result<HealthConversation> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
            android.util.Log.d("HealthAssistantRepo", "Creating new health conversation for userId = $userId")
            val conversation = DbHealthConversation(
                userId = userId,
                title = title,
                language = language
            )
            val result = postgrest.from("health_conversations").insert(conversation) {
                select()
            }.decodeSingle<DbHealthConversation>()
            
            val domain = result.asDomain()
            android.util.Log.d("HealthAssistantRepo", "Created conversation ID = ${domain.id}")
            domain
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        message: String,
        language: String
    ): Result<HealthAssistantResponseDto> = withContext(Dispatchers.IO) {
        runCatching {
            android.util.Log.d("HealthAssistantRepo", "Edge Function request conversationId = $conversationId")
            val request = HealthAssistantRequest(
                conversationId = conversationId,
                message = message,
                language = language
            )
            // Use the non-generic invoke and manually decode with Ktor's body()
            val response = functions.invoke("health-assistant", body = request)
            val result = response.body<HealthAssistantResponseDto>()
            android.util.Log.d("HealthAssistantRepo", "Received response for conversationId = ${result.conversationId}")
            result
        }.recoverCatching { e ->
            val errorMessage = when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true || e.message?.contains("404") == true -> 
                    "CONVERSATION_NOT_FOUND"
                e.message?.contains("429") == true -> "AI service is temporarily unavailable. Please try again later."
                e.message?.contains("401") == true || e.message?.contains("403") == true -> "Your session has expired. Please sign in again."
                e.message?.contains("timeout", ignoreCase = true) == true || e is HttpRequestTimeoutException -> "Request timed out. Please try again."
                e is java.net.ConnectException || e is java.net.UnknownHostException -> "Unable to connect. Please check your internet connection."
                e.message?.contains("500") == true || e.message?.contains("503") == true -> "AI service is temporarily unavailable. Please try again later."
                else -> e.localizedMessage ?: "Unable to send message"
            }
            throw Exception(errorMessage)
        }
    }

    suspend fun getConversations(): Result<List<HealthConversation>> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
            postgrest.from("health_conversations")
                .select {
                    filter { eq("user_id", userId) }
                    order("updated_at", Order.DESCENDING)
                }
                .decodeList<DbHealthConversation>()
                .map { it.asDomain() }
        }
    }

    suspend fun getConversation(
        conversationId: String
    ): Result<HealthConversation> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("health_conversations")
                .select {
                    filter { eq("id", conversationId) }
                }
                .decodeSingle<DbHealthConversation>()
                .asDomain()
        }
    }

    suspend fun getMessages(
        conversationId: String
    ): Result<List<HealthMessage>> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("health_messages")
                .select {
                    filter { eq("conversation_id", conversationId) }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<DbHealthMessage>()
                .map { it.asDomain() }
        }
    }

    suspend fun deleteConversation(
        conversationId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("health_conversations").delete {
                filter { eq("id", conversationId) }
            }
            Unit
        }
    }
}
