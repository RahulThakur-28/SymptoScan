package com.rahul.symptoscan.presentation.ai.state

import com.rahul.symptoscan.domain.model.AssessmentSummary
import com.rahul.symptoscan.presentation.ai.model.ChatMessage

data class AiAssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val suggestedQuestions: List<String> = emptyList(),
    val latestAssessmentContext: AssessmentSummary? = null,
    val isTyping: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false
)
