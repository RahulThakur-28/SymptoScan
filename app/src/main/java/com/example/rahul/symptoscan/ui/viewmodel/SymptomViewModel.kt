package com.example.rahul.symptoscan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahul.symptoscan.MyApp
import com.example.rahul.symptoscan.data.local.SymptomEntity
import com.example.rahul.symptoscan.data.remote.SymptomAnalysis
import com.example.rahul.symptoscan.data.repository.SymptomRepository
import com.example.rahul.symptoscan.data.remote.RetrofitInstance
import com.example.rahul.symptoscan.data.local.AppDatabase
import com.example.rahul.symptoscan.ui.screens.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI STATE
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val data: SymptomAnalysis) : UiState()
    data class Error(val message: String) : UiState()
}

class SymptomViewModel : ViewModel() {

    // Repository
    private val repository = SymptomRepository(
        symptomDao = AppDatabase.getDatabase(MyApp.context).symptomDao(),
        apiService = RetrofitInstance.api,
        auth = FirebaseAuth.getInstance(),
        firestore = FirebaseFirestore.getInstance()
    )

    // STATE
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    val history: Flow<List<SymptomEntity>> = repository.getAllHistory()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    // ================= ANALYZE =================
    fun analyzeSymptoms(symptoms: String) {

        if (symptoms.isBlank()) {
            _uiState.value = UiState.Error("Please enter symptoms")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {

            _uiState.value = UiState.Loading

            try {
                val result = repository.analyzeSymptoms(symptoms)

                // SAVE
                repository.saveSymptomResult(
                    SymptomEntity(
                        symptoms = symptoms,
                        result = result.conditions.joinToString(", "),
                        riskLevel = result.riskLevel,
                        date = System.currentTimeMillis()
                    )
                )

                _uiState.value = UiState.Success(result)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }


    // ================= CHAT =================
    fun sendMessage(message: String) {

        if (message.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {

            // User message
            _chatMessages.value =
                _chatMessages.value + ChatMessage(message, true)

            _isLoading.value = true

            try {
                val reply = repository.getChatResponse(message)

                // AI reply
                _chatMessages.value =
                    _chatMessages.value + ChatMessage(reply, false)

                // 🔥 CLEAN TEXT
                val cleanReply = reply
                    .replace("###", "")
                    .replace("**", "")
                    .replace("*", "")
                    .replace("\n", " ")

                // 🔥 EXTRACT RISK
                val risk = when {
                    reply.contains("high", true) -> "High"
                    reply.contains("medium", true) -> "Medium"
                    reply.contains("low", true) -> "Low"
                    else -> "Unknown"
                }

                // 🔥 SAVE TO DB (MAIN FIX)
                repository.saveSymptomResult(
                    SymptomEntity(
                        symptoms = message,
                        result = cleanReply,
                        riskLevel = risk,
                        date = System.currentTimeMillis()
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _chatMessages.value =
                    _chatMessages.value + ChatMessage("Error getting response", false)
            }

            _isLoading.value = false
        }
    }
}