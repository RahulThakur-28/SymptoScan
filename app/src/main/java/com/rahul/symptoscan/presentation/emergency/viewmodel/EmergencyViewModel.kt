package com.rahul.symptoscan.presentation.emergency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.EmergencyContactRepository
import com.rahul.symptoscan.presentation.emergency.state.EmergencyUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmergencyViewModel(
    private val repository: EmergencyContactRepository = Injection.emergencyContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    init {
        loadEmergencyContact()
    }

    fun loadEmergencyContact() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getEmergencyContact()
                .onSuccess { contact ->
                    _uiState.update { it.copy(emergencyContact = contact, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun removeEmergencyContact() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.deleteEmergencyContact()
                .onSuccess {
                    _uiState.update { it.copy(emergencyContact = null, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
