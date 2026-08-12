package com.rahul.symptoscan.presentation.emergency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.EmergencyContactRepository
import com.rahul.symptoscan.domain.model.EmergencyContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEmergencyContactViewModel(
    private val repository: EmergencyContactRepository = Injection.emergencyContactRepository,
    private val authRepository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEmergencyContactUiState())
    val uiState: StateFlow<AddEmergencyContactUiState> = _uiState.asStateFlow()

    fun loadExistingContact() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getEmergencyContact().onSuccess { contact ->
                if (contact != null) {
                    _uiState.update { it.copy(
                        name = contact.name,
                        phoneNumber = contact.phoneNumber,
                        relationship = contact.relationship,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun onRelationshipChange(relationship: String) {
        _uiState.update { it.copy(relationship = relationship) }
    }

    fun saveContact(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank() || state.phoneNumber.isBlank() || state.relationship.isBlank()) {
            _uiState.update { it.copy(error = "Please fill all required fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUser()?.id ?: return@launch
            val contact = EmergencyContact(
                userId = userId,
                name = state.name,
                phoneNumber = state.phoneNumber,
                relationship = state.relationship
            )
            repository.saveEmergencyContact(contact)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    data class AddEmergencyContactUiState(
        val name: String = "",
        val phoneNumber: String = "",
        val relationship: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
