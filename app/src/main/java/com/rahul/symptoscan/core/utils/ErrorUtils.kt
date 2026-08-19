package com.rahul.symptoscan.core.utils

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import java.net.ConnectException
import java.net.UnknownHostException

object ErrorUtils {
    
    /**
     * Maps a technical exception into a user-friendly message.
     * Prevents leaking URLs, database internals, and stack traces to the UI.
     */
    fun getUserFriendlyMessage(throwable: Throwable?): String {
        if (throwable == null) return "An unexpected error occurred. Please try again."

        val message = throwable.message ?: ""
        
        return when {
            // Network & Timeouts
            throwable is HttpRequestTimeoutException || message.contains("timeout", ignoreCase = true) -> 
                "The request timed out. Please check your internet connection and try again."
            
            throwable is ConnectException || throwable is UnknownHostException ->
                "Unable to connect to the server. Please check your internet and try again."

            // Supabase / Postgrest technical errors
            message.contains("PGRST", ignoreCase = true) || 
            message.contains("Postgrest", ignoreCase = true) ||
            message.contains("database", ignoreCase = true) ||
            message.contains("column", ignoreCase = true) ||
            message.contains("table", ignoreCase = true) ->
                "Unable to process your request at the moment. Please try again later."

            // Auth errors
            message.contains("invalid_credentials", ignoreCase = true) ||
            message.contains("invalid login credentials", ignoreCase = true) ->
                "Invalid email or password. Please try again."

            message.contains("user_not_found", ignoreCase = true) ->
                "Account not found. Please sign up."

            // AI related
            message.contains("Gemini", ignoreCase = true) || 
            message.contains("AI_ERROR", ignoreCase = true) ->
                "The AI assistant is temporarily unavailable. Please try again in a moment."
            
            message.contains("QUOTA_EXCEEDED", ignoreCase = true) ->
                "AI service is busy. Please try again later."

            // Generic Sanitization: Remove URLs from any string
            message.contains("http://") || message.contains("https://") || message.contains(".supabase.") ->
                "A network error occurred. Please try again."

            // Fallback for specific known messages from our Edge Functions
            message.isNotBlank() && message.length < 100 && !isTechnical(message) -> message

            else -> "Something went wrong. Please try again."
        }
    }

    private fun isTechnical(message: String): Boolean {
        val technicalKeywords = listOf("ReferenceError", "TypeError", "NullPointerException", "IndexOutOfBounds", "404", "500", "503", "{", "}")
        return technicalKeywords.any { message.contains(it, ignoreCase = true) }
    }
}
