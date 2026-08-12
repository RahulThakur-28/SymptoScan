package com.rahul.symptoscan.presentation.ai.state

import com.rahul.symptoscan.domain.model.HealthConversation
import com.rahul.symptoscan.domain.model.HealthMessage

data class HealthAssistantUiState(
    val messages: List<HealthMessage> = emptyList(),
    val conversations: List<HealthConversation> = emptyList(),
    val currentConversationId: String? = null,
    val inputText: String = "",
    val language: String = "en",
    val isLoading: Boolean = false,
    val isLoadingMessages: Boolean = false,
    val isLoadingConversations: Boolean = false,
    val isSendingMessage: Boolean = false,
    val error: String? = null,
    val isConversationCreated: Boolean = false
)
