package com.rahul.symptoscan.presentation.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.HealthAssistantRepository
import com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HealthAssistantViewModel(
    private val repository: HealthAssistantRepository = Injection.healthAssistantRepository
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

    fun createConversation() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.createConversation(
                title = "New Health Conversation",
                language = _uiState.value.language
            ).onSuccess { conversation ->
                _uiState.update { 
                    it.copy(
                        currentConversationId = conversation.id,
                        isConversationCreated = true,
                        messages = emptyList(),
                        isLoading = false
                    ) 
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isSendingMessage) return

        viewModelScope.launch {
            var conversationId = _uiState.value.currentConversationId

            if (conversationId == null) {
                _uiState.update { it.copy(isSendingMessage = true, error = null) }
                val result = repository.createConversation(
                    title = text.take(30) + if (text.length > 30) "..." else "",
                    language = _uiState.value.language
                )
                
                result.onSuccess { conversation ->
                    conversationId = conversation.id
                    _uiState.update { 
                        it.copy(
                            currentConversationId = conversation.id,
                            isConversationCreated = true
                        ) 
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isSendingMessage = false, error = e.message) }
                    return@launch
                }
            } else {
                _uiState.update { it.copy(isSendingMessage = true, error = null) }
            }

            conversationId?.let { id ->
                repository.sendMessage(
                    conversationId = id,
                    message = text,
                    language = _uiState.value.language
                ).onSuccess {
                    // Success, reload messages to get the latest state (user + assistant message)
                    loadMessages(id)
                    _uiState.update { it.copy(inputText = "", isSendingMessage = false) }
                }.onFailure { e ->
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
