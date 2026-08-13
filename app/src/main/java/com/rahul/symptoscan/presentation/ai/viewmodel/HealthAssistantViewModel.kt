package com.rahul.symptoscan.presentation.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.HealthAssistantRepository
import com.rahul.symptoscan.domain.model.HealthMessage
import com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HealthAssistantViewModel(
    private val repository: HealthAssistantRepository = Injection.healthAssistantRepository,
    private val authRepository: com.rahul.symptoscan.data.repository.AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthAssistantUiState())
    val uiState: StateFlow<HealthAssistantUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        _uiState.update { it.copy(isLoadingConversations = true, error = null) }
        viewModelScope.launch {
            repository.getConversations()
                .onSuccess { conversations ->
                    _uiState.update { it.copy(conversations = conversations, isLoadingConversations = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoadingConversations = false) }
                }
        }
    }

    fun initializeConversation(conversationId: String?) {
        if (conversationId != null && conversationId != _uiState.value.currentConversationId) {
            openConversation(conversationId)
        } else if (conversationId == null && _uiState.value.currentConversationId != null) {
            startNewConversation()
        }
    }

    fun startNewConversation() {
        _uiState.update {
            it.copy(
                currentConversationId = null,
                messages = emptyList(),
                inputText = "",
                error = null,
                isConversationCreated = false
            )
        }
    }

    fun openConversation(conversationId: String) {
        _uiState.update { it.copy(currentConversationId = conversationId, isLoadingMessages = true, error = null) }
        viewModelScope.launch {
            repository.getMessages(conversationId)
                .onSuccess { messages ->
                    _uiState.update { it.copy(messages = messages, isLoadingMessages = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoadingMessages = false) }
                }
        }
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(inputText = value) }
    }

    fun setLanguage(language: String) {
        if (language == "en" || language == "hi") {
            _uiState.update { it.copy(language = language) }
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isSendingMessage) return

        val userId = authRepository.getCurrentUser()?.id ?: ""

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingMessage = true, error = null) }
            
            // Optimistic update if we already have a conversation
            if (_uiState.value.currentConversationId != null) {
                val tempMsg = HealthMessage(
                    id = "temp_${System.currentTimeMillis()}",
                    conversationId = _uiState.value.currentConversationId!!,
                    userId = userId,
                    role = "user",
                    content = text,
                    createdAt = null
                )
                _uiState.update { it.copy(messages = it.messages + tempMsg, inputText = "") }
            }

            // 1. Resolve or Create Conversation ID
            val activeConversationId = when (val currentId = _uiState.value.currentConversationId) {
                null -> {
                    android.util.Log.d("HealthAssistantVM", "currentConversationId is null, creating new")
                    val result = repository.createConversation(
                        title = text.take(50) + if (text.length > 50) "..." else "",
                        language = _uiState.value.language
                    )
                    
                    val newConv = result.getOrElse { e ->
                        android.util.Log.e("HealthAssistantVM", "Failed to create conversation", e)
                        _uiState.update { it.copy(isSendingMessage = false, error = e.message) }
                        return@launch
                    }
                    
                    if (newConv.id.isBlank()) {
                        android.util.Log.e("HealthAssistantVM", "Created conversation ID is blank")
                        _uiState.update { it.copy(isSendingMessage = false, error = "Failed to create conversation session.") }
                        return@launch
                    }

                    android.util.Log.d("HealthAssistantVM", "Created conversation successfully with ID: ${newConv.id}")
                    
                    // Show message optimistically for the NEW conversation now that we have ID
                    val tempMsg = HealthMessage(
                        id = "temp_${System.currentTimeMillis()}",
                        conversationId = newConv.id,
                        userId = userId,
                        role = "user",
                        content = text,
                        createdAt = null
                    )
                    
                    _uiState.update { 
                        it.copy(
                            currentConversationId = newConv.id,
                            isConversationCreated = true,
                            messages = it.messages + tempMsg,
                            inputText = ""
                        ) 
                    }
                    newConv.id
                }
                else -> {
                    android.util.Log.d("HealthAssistantVM", "Using existing conversation ID: $currentId")
                    currentId
                }
            }

            android.util.Log.d("HealthAssistantVM", "Sending message with conversationId: $activeConversationId")

            // 2. Send Message using the verified ID
            repository.sendMessage(
                conversationId = activeConversationId,
                message = text,
                language = _uiState.value.language
            ).onSuccess {
                // Success, reload messages to get the latest state (user + assistant message)
                loadMessages(activeConversationId)
                _uiState.update { it.copy(isSendingMessage = false) }
            }.onFailure { e ->
                if (e.message == "CONVERSATION_NOT_FOUND") {
                    _uiState.update { 
                        it.copy(
                            isSendingMessage = false, 
                            error = "Conversation could not be found. Please start a new chat.",
                            currentConversationId = null,
                            messages = emptyList(),
                            isConversationCreated = false
                        ) 
                    }
                } else {
                    // Remove the optimistic message on failure? 
                    // Better to just show error.
                    _uiState.update { it.copy(isSendingMessage = false, error = e.message) }
                }
            }
        }
    }

    private suspend fun loadMessages(conversationId: String) {
        repository.getMessages(conversationId).onSuccess { messages ->
            _uiState.update { it.copy(messages = messages) }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId).onSuccess {
                _uiState.update { state ->
                    val newList = state.conversations.filter { it.id != conversationId }
                    if (state.currentConversationId == conversationId) {
                        state.copy(
                            conversations = newList,
                            currentConversationId = null,
                            messages = emptyList(),
                            isConversationCreated = false
                        )
                    } else {
                        state.copy(conversations = newList)
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun refreshConversations() {
        loadConversations()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun retry() {
        // Simple retry: if we have an input text and no active send, try sending again.
        // If we are opening a conversation, try loading messages.
        val state = _uiState.value
        if (state.inputText.isNotBlank() && !state.isSendingMessage) {
            sendMessage()
        } else if (state.currentConversationId != null && state.messages.isEmpty()) {
            openConversation(state.currentConversationId)
        } else {
            loadConversations()
        }
    }
}
