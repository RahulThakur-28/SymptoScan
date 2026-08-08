package com.rahul.symptoscan.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.domain.model.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val profileRepository: ProfileRepository = Injection.profileRepository,
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
        val userId = authRepository.getCurrentUser()?.id ?: return
        viewModelScope.launch {
            profileRepository.getUserProfile(userId)
                .collect { _profile.value = it }
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
        _profile.update { it?.copy(height = height.filter { char -> char.isDigit() }.toIntOrNull()) }
    }

    fun onWeightChange(weight: String) {
        _profile.update { it?.copy(weight = weight.filter { char -> char.isDigit() }.toIntOrNull()) }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val currentProfile = _profile.value ?: return
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            val result = profileRepository.updateProfile(currentProfile)
            if (result.isSuccess) {
                _uiState.value = EditProfileUiState.Success
                onSuccess()
            } else {
                _uiState.value = EditProfileUiState.Error(result.exceptionOrNull()?.message ?: "Update failed")
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
