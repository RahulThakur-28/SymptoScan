package com.rahul.symptoscan.presentation.healthprofile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.EmergencyContactRepository
import com.rahul.symptoscan.data.repository.HealthProfileRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.domain.model.EmergencyContact
import com.rahul.symptoscan.domain.model.HealthProfile
import com.rahul.symptoscan.domain.model.UserProfile
import com.rahul.symptoscan.presentation.healthprofile.event.HealthProfileEvent
import com.rahul.symptoscan.presentation.healthprofile.state.HealthProfileUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthProfileViewModel(
    private val profileRepository: ProfileRepository = Injection.profileRepository,
    private val healthProfileRepository: HealthProfileRepository = Injection.healthProfileRepository,
    private val emergencyRepository: EmergencyContactRepository = Injection.emergencyContactRepository,
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
            combine(
                profileRepository.getUserProfile(user.id).take(1),
                healthProfileRepository.getHealthProfile(user.id).take(1),
                flow { emit(emergencyRepository.getEmergencyContact().getOrNull()) }.take(1)
            ) { profile: com.rahul.symptoscan.domain.model.UserProfile?, 
                healthProfile: com.rahul.symptoscan.domain.model.HealthProfile?, 
                emergencyContact: EmergencyContact? ->
                profile?.let { p ->
                    _uiState.update { it.copy(
                        userProfile = p,
                        fullName = p.fullName,
                    ) }
                }
                healthProfile?.let { hp ->
                    _uiState.update { it.copy(
                        dob = hp.dateOfBirth ?: "",
                        gender = hp.biologicalSex ?: "",
                        height = hp.heightCm?.toString() ?: "",
                        weight = hp.weightKg?.toString() ?: "",
                        bloodGroup = hp.bloodGroup ?: "",
                        selectedAllergies = hp.allergies?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                        conditions = hp.medicalConditions?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                        medications = hp.medications ?: "",
                        profileCompleted = hp.profileCompleted,
                        // Auto-advance to medical step if basic info exists but profile not fully complete
                        currentStep = if (!hp.profileCompleted && hp.dateOfBirth != null) 3 else it.currentStep
                    ) }
                }
                emergencyContact?.let { ec ->
                    _uiState.update { it.copy(
                        emergencyContactName = ec.name,
                        emergencyRelationship = ec.relationship,
                        emergencyPhone = ec.phoneNumber
                    ) }
                }
            }.collect()
        }
    }

    fun onEvent(event: HealthProfileEvent) {
        android.util.Log.d("HealthProfile", "[HEALTH-EVENT] Received event: ${event::class.simpleName}")
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
            HealthProfileEvent.Complete -> saveProfile(isFinal = true)
            HealthProfileEvent.SaveBasic -> saveProfile(isFinal = false)
            is HealthProfileEvent.StepChanged -> _uiState.update { it.copy(currentStep = event.step) }
        }
    }

    private fun saveProfile(isFinal: Boolean) {
        android.util.Log.d("HealthProfile", "[HEALTH-SAVE-2] ViewModel save started. isFinal: $isFinal")
        
        val currentState = _uiState.value
        val user = authRepository.getCurrentUser()
        
        if (user == null) {
            android.util.Log.e("HealthProfile", "[HEALTH-SAVE-ERROR] Auth user not found")
            _uiState.update { it.copy(error = "Authentication required. Please login again.") }
            return
        }

        android.util.Log.d("HealthProfile", "[HEALTH-SAVE-3] Auth user obtained: ${user.id}")

        // Validate basic requirements for Phase 1
        // We only require Step 1 data if we are in Basic Setup or if it's currently missing
        if (!isFinal && (currentState.dob.isBlank() || currentState.gender.isBlank())) {
            android.util.Log.e("HealthProfile", "[HEALTH-SAVE-ERROR] Validation failed: Step 1 data missing. State: $currentState")
            _uiState.update { it.copy(error = "Please complete Step 1 (Personal Info) before saving.") }
            return
        }

        if (currentState.height.isBlank() || currentState.weight.isBlank()) {
            android.util.Log.e("HealthProfile", "[HEALTH-SAVE-ERROR] Validation failed: Height or Weight missing")
            _uiState.update { it.copy(error = "Please enter your height and weight.") }
            return
        }

        android.util.Log.d("HealthProfile", "[HEALTH-SAVE-4] Validation passed")

        // Prepare domain models
        val updatedUserProfile = (currentState.userProfile ?: UserProfile(
            id = user.id,
            fullName = currentState.fullName,
            email = user.email ?: "",
            memberSince = "Just now"
        )).copy(
            fullName = currentState.fullName
        )

        val emergencyContact = EmergencyContact(
            userId = user.id,
            name = currentState.emergencyContactName,
            relationship = currentState.emergencyRelationship,
            phoneNumber = currentState.emergencyPhone
        )

        val healthProfile = HealthProfile(
            userId = user.id,
            dateOfBirth = currentState.dob,
            biologicalSex = currentState.gender,
            heightCm = currentState.height.toDoubleOrNull(),
            weightKg = currentState.weight.toDoubleOrNull(),
            bloodGroup = currentState.bloodGroup,
            allergies = currentState.selectedAllergies.joinToString(","),
            medicalConditions = currentState.conditions.joinToString(","),
            medications = currentState.medications,
            profileCompleted = isFinal || currentState.profileCompleted
        )

        viewModelScope.launch {
            android.util.Log.d("HealthProfile", "[HEALTH-SAVE-5] Database save starting...")
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val profileTask = profileRepository.updateProfile(updatedUserProfile)
            android.util.Log.d("HealthProfile", "[HEALTH-SAVE-5.1] Profile update task finished: ${profileTask.isSuccess}")

            val healthTask = if (isFinal) {
                healthProfileRepository.saveAdditionalHealthProfile(healthProfile)
            } else {
                healthProfileRepository.saveBasicHealthProfile(healthProfile)
            }
            android.util.Log.d("HealthProfile", "[HEALTH-SAVE-5.2] Health profile save task finished: ${healthTask.isSuccess}")
            
            val emergencyTask = if (currentState.emergencyContactName.isNotBlank()) {
                emergencyRepository.saveEmergencyContact(emergencyContact)
            } else {
                Result.success(Unit)
            }
            android.util.Log.d("HealthProfile", "[HEALTH-SAVE-5.3] Emergency contact save task finished: ${emergencyTask.isSuccess}")

            if (profileTask.isSuccess && healthTask.isSuccess && emergencyTask.isSuccess) {
                android.util.Log.d("HealthProfile", "[HEALTH-SAVE-6] Database save successful")
                _uiState.update { it.copy(
                    isLoading = false, 
                    isComplete = true, 
                    profileCompleted = healthProfile.profileCompleted 
                ) }
                android.util.Log.d("HealthProfile", "[HEALTH-SAVE-7] Navigation triggered (isComplete = true)")
            } else {
                val errorMsg = when {
                    profileTask.isFailure -> "Profile Error: ${profileTask.exceptionOrNull()?.message}"
                    healthTask.isFailure -> "Health Profile Error: ${healthTask.exceptionOrNull()?.message}"
                    emergencyTask.isFailure -> "Emergency Contact Error: ${emergencyTask.exceptionOrNull()?.message}"
                    else -> "Unable to save your health profile. Please try again."
                }
                android.util.Log.e("HealthProfile", "[HEALTH-SAVE-ERROR] $errorMsg")
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }
}
