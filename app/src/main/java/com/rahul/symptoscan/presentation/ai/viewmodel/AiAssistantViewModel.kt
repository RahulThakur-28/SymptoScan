package com.rahul.symptoscan.presentation.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AssessmentRepository
import com.rahul.symptoscan.presentation.ai.model.ChatMessage
import com.rahul.symptoscan.presentation.ai.model.MessageRole
import com.rahul.symptoscan.presentation.ai.state.AiAssistantUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AiAssistantViewModel(
    private val assessmentRepository: AssessmentRepository = Injection.assessmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    init {
        loadLatestAssessmentContext()
        loadSuggestedQuestions()
    }

    private fun loadLatestAssessmentContext() {
        viewModelScope.launch {
            assessmentRepository.getAssessmentHistory()
                .map { it.firstOrNull() }
                .collect { assessment ->
                    _uiState.update { it.copy(latestAssessmentContext = assessment) }
                }
        }
    }

    private fun loadSuggestedQuestions() {
        val questions = listOf(
            "What does my risk score mean?",
            "When should I see a doctor?",
            "Can stress cause my symptoms?",
            "What do my assessment results mean?"
        )
        _uiState.update { it.copy(suggestedQuestions = questions) }
    }

    fun onMessageChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        sendMessage(text)
    }

    fun onSuggestedQuestionClicked(question: String) {
        sendMessage(question)
    }

    private fun sendMessage(content: String) {
        viewModelScope.launch {
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = MessageRole.USER,
                content = content
            )

            _uiState.update { 
                it.copy(
                    messages = it.messages + userMessage,
                    inputText = "",
                    isTyping = true
                )
            }

            // Simulating AI response
            delay(2000)
            
            val aiResponse = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = MessageRole.ASSISTANT,
                content = "I'm your AI health assistant. Based on your question: \"$content\", I can provide general health education. Remember, I'm not a doctor and this is not a medical diagnosis."
            )

            _uiState.update { 
                it.copy(
                    messages = it.messages + aiResponse,
                    isTyping = false
                )
            }
        }
    }

    fun onDeleteConversationClicked() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun onConfirmDelete() {
        _uiState.update { 
            it.copy(
                messages = emptyList(),
                showDeleteConfirmation = false
            )
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }
    
    fun onRetry() {
        // Implement retry logic if needed
    }
}
