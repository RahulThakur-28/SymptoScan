package com.rahul.symptoscan.presentation.auth.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.domain.model.UserProfile
import com.rahul.symptoscan.presentation.auth.profile.event.BasicProfileEvent
import com.rahul.symptoscan.presentation.auth.profile.state.BasicProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class BasicProfileViewModel(
    private val profileRepository: ProfileRepository = Injection.profileRepository,
    private val authRepository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BasicProfileState())
    val state: StateFlow<BasicProfileState> = _state.asStateFlow()

    fun onEvent(event: BasicProfileEvent) {
        when (event) {
            is BasicProfileEvent.AgeChanged -> _state.update { it.copy(age = event.age.filter { char -> char.isDigit() }) }
            is BasicProfileEvent.GenderChanged -> _state.update { it.copy(gender = event.gender) }
            is BasicProfileEvent.HeightChanged -> _state.update { it.copy(height = event.height.filter { char -> char.isDigit() }) }
            is BasicProfileEvent.WeightChanged -> _state.update { it.copy(weight = event.weight.filter { char -> char.isDigit() }) }
            is BasicProfileEvent.BloodGroupChanged -> _state.update { it.copy(bloodGroup = event.bloodGroup) }
            is BasicProfileEvent.ContinueClicked -> saveProfile()
        }
    }

    private fun saveProfile() {
        val currentState = _state.value
        if (!currentState.isContinueEnabled) return
        
        val user = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val fullName = user.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: "User"

            val profile = UserProfile(
                id = user.id,
                fullName = fullName,
                email = user.email ?: "",
                memberSince = "Aug '24",
                age = currentState.age.toIntOrNull(),
                gender = currentState.gender,
                height = currentState.height.toIntOrNull(),
                weight = currentState.weight.toIntOrNull(),
                bloodGroup = currentState.bloodGroup
            )

            val result = profileRepository.updateProfile(profile)
            
            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
