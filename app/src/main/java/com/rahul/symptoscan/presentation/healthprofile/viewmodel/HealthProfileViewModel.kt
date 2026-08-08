package com.rahul.symptoscan.presentation.healthprofile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.domain.model.EmergencyContact
import com.rahul.symptoscan.domain.model.UserProfile
import com.rahul.symptoscan.presentation.healthprofile.event.HealthProfileEvent
import com.rahul.symptoscan.presentation.healthprofile.state.HealthProfileUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthProfileViewModel(
    private val profileRepository: ProfileRepository = Injection.profileRepository,
    private val authRepository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthProfileUiState())
    val uiState: StateFlow<HealthProfileUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            profileRepository.getUserProfile(user.id).collect { profile ->
                _uiState.update { it.copy(
                    userProfile = profile,
                    fullName = profile.fullName,
                    dob = profile.dob ?: "",
                    gender = profile.gender ?: "",
                    height = profile.height?.toString() ?: "",
                    weight = profile.weight?.toString() ?: "",
                    bloodGroup = profile.bloodGroup ?: "",
                    selectedAllergies = profile.allergies,
                    conditions = profile.conditions,
                    medications = profile.medications ?: "",
                    emergencyContactName = profile.emergencyContact?.name ?: "",
                    emergencyRelationship = profile.emergencyContact?.relationship ?: "",
                    emergencyPhone = profile.emergencyContact?.phoneNumber ?: ""
                ) }
            }
        }
    }

    fun onEvent(event: HealthProfileEvent) {
        when (event) {
            is HealthProfileEvent.FullNameChanged -> _uiState.update { it.copy(fullName = event.name) }
            is HealthProfileEvent.DobChanged -> _uiState.update { it.copy(dob = event.dob) }
            is HealthProfileEvent.GenderChanged -> _uiState.update { it.copy(gender = event.gender) }
            is HealthProfileEvent.HeightChanged -> _uiState.update { it.copy(height = event.height.filter { it.isDigit() }) }
            is HealthProfileEvent.WeightChanged -> _uiState.update { it.copy(weight = event.weight.filter { it.isDigit() }) }
            is HealthProfileEvent.BloodGroupChanged -> _uiState.update { it.copy(bloodGroup = event.group) }
            is HealthProfileEvent.AllergyToggled -> {
                _uiState.update { state ->
                    val newList = if (state.selectedAllergies.contains(event.allergy)) {
                        state.selectedAllergies - event.allergy
                    } else {
                        state.selectedAllergies + event.allergy
                    }
                    state.copy(selectedAllergies = newList)
                }
            }
            is HealthProfileEvent.CustomAllergyAdded -> {
                _uiState.update { it.copy(selectedAllergies = it.selectedAllergies + event.allergy) }
            }
            is HealthProfileEvent.ConditionAdded -> {
                _uiState.update { it.copy(conditions = it.conditions + event.condition) }
            }
            is HealthProfileEvent.ConditionRemoved -> {
                _uiState.update { it.copy(conditions = it.conditions - event.condition) }
            }
            is HealthProfileEvent.MedicationsChanged -> _uiState.update { it.copy(medications = event.meds) }
            is HealthProfileEvent.EmergencyContactNameChanged -> _uiState.update { it.copy(emergencyContactName = event.name) }
            is HealthProfileEvent.EmergencyRelationshipChanged -> _uiState.update { it.copy(emergencyRelationship = event.rel) }
            is HealthProfileEvent.EmergencyPhoneChanged -> _uiState.update { it.copy(emergencyPhone = event.phone) }
            HealthProfileEvent.Back -> {
                if (_uiState.value.currentStep > 1) {
                    _uiState.update { it.copy(currentStep = it.currentStep - 1) }
                }
            }
            HealthProfileEvent.Continue -> {
                if (_uiState.value.currentStep < 4) {
                    _uiState.update { it.copy(currentStep = it.currentStep + 1) }
                }
            }
            HealthProfileEvent.Complete -> saveProfile()
            is HealthProfileEvent.StepChanged -> _uiState.update { it.copy(currentStep = event.step) }
        }
    }

    private fun saveProfile() {
        val state = _uiState.value
        val baseProfile = state.userProfile ?: return
        
        val updatedProfile = baseProfile.copy(
            fullName = state.fullName,
            dob = state.dob,
            gender = state.gender,
            height = state.height.toIntOrNull(),
            weight = state.weight.toIntOrNull(),
            bloodGroup = state.bloodGroup,
            allergies = state.selectedAllergies,
            conditions = state.conditions,
            medications = state.medications,
            emergencyContact = EmergencyContact(
                name = state.emergencyContactName,
                relationship = state.emergencyRelationship,
                phoneNumber = state.emergencyPhone
            )
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = profileRepository.updateProfile(updatedProfile)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isComplete = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to save profile") }
            }
        }
    }
}
