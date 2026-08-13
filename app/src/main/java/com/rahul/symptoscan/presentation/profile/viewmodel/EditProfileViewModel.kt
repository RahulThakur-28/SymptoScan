package com.rahul.symptoscan.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.HealthProfileRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.domain.model.HealthProfile
import com.rahul.symptoscan.domain.model.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val profileRepository: ProfileRepository = Injection.profileRepository,
    private val healthProfileRepository: HealthProfileRepository = Injection.healthProfileRepository,
    private val authRepository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val currentUser = authRepository.getCurrentUser() ?: return
        val userId = currentUser.id
        val email = currentUser.email ?: ""

        viewModelScope.launch {
            combine(
                profileRepository.getUserProfile(userId),
                healthProfileRepository.getHealthProfile(userId)
            ) { base, health ->
                base?.copy(
                    email = email,
                    isVerified = true,
                    dob = health?.dateOfBirth,
                    gender = health?.biologicalSex,
                    bloodGroup = health?.bloodGroup,
                    height = health?.heightCm,
                    weight = health?.weightKg,
                    allergies = health?.allergies,
                    conditions = health?.medicalConditions,
                    medications = health?.medications,
                    isProfileComplete = health?.profileCompleted ?: false
                )
            }.collect { _profile.value = it }
        }
    }

    fun onNameChange(name: String) {
        _profile.update { it?.copy(fullName = name) }
    }

    fun onGenderChange(gender: String) {
        _profile.update { it?.copy(gender = gender) }
    }

    fun onBloodGroupChange(group: String) {
        _profile.update { it?.copy(bloodGroup = group) }
    }

    fun onHeightChange(height: String) {
        _profile.update { it?.copy(height = height.toDoubleOrNull()) }
    }

    fun onWeightChange(weight: String) {
        _profile.update { it?.copy(weight = weight.toDoubleOrNull()) }
    }

    fun onDobChange(dob: String) {
        _profile.update { it?.copy(dob = dob) }
    }

    fun onAllergiesChange(allergies: String) {
        _profile.update { it?.copy(allergies = allergies) }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val currentProfile = _profile.value ?: return
        
        // Validation
        if (currentProfile.fullName.isBlank()) {
            _uiState.value = EditProfileUiState.Error("Name cannot be empty")
            return
        }

        if (currentProfile.dob.isNullOrBlank()) {
            _uiState.value = EditProfileUiState.Error("Please select your date of birth")
            return
        }

        if (currentProfile.height != null && (currentProfile.height <= 0 || currentProfile.height > 300)) {
            _uiState.value = EditProfileUiState.Error("Please enter a valid height")
            return
        }

        if (currentProfile.weight != null && (currentProfile.weight <= 0 || currentProfile.weight > 600)) {
            _uiState.value = EditProfileUiState.Error("Please enter a valid weight")
            return
        }

        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            
            // 1. Update basic profile
            val updateResult = profileRepository.updateProfile(currentProfile)
            if (updateResult.isFailure) {
                _uiState.value = EditProfileUiState.Error(updateResult.exceptionOrNull()?.message ?: "Update failed")
                return@launch
            }

            // 2. Update health profile
            val isComplete = !currentProfile.fullName.isBlank() &&
                             !currentProfile.dob.isNullOrBlank() &&
                             !currentProfile.gender.isNullOrBlank() &&
                             currentProfile.height != null &&
                             currentProfile.weight != null &&
                             !currentProfile.bloodGroup.isNullOrBlank()

            val healthProfile = HealthProfile(
                userId = currentProfile.id,
                dateOfBirth = currentProfile.dob,
                biologicalSex = currentProfile.gender,
                heightCm = currentProfile.height,
                weightKg = currentProfile.weight,
                bloodGroup = currentProfile.bloodGroup,
                allergies = currentProfile.allergies,
                medications = currentProfile.medications,
                medicalConditions = currentProfile.conditions,
                profileCompleted = isComplete
            )
            
            val healthResult = healthProfileRepository.saveHealthProfile(healthProfile)
            if (healthResult.isSuccess) {
                _uiState.value = EditProfileUiState.Success
                onSuccess()
            } else {
                _uiState.value = EditProfileUiState.Error(healthResult.exceptionOrNull()?.message ?: "Update failed")
            }
        }
    }

    sealed class EditProfileUiState {
        object Idle : EditProfileUiState()
        object Loading : EditProfileUiState()
        object Success : EditProfileUiState()
        data class Error(val message: String) : EditProfileUiState()
    }
}
