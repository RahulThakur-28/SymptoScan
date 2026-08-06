package com.rahul.symptoscan.presentation.auth.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.presentation.auth.profile.event.BasicProfileEvent
import com.rahul.symptoscan.presentation.auth.profile.state.BasicProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BasicProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(BasicProfileState())
    val state: StateFlow<BasicProfileState> = _state.asStateFlow()

    fun onEvent(event: BasicProfileEvent) {
        when (event) {
            is BasicProfileEvent.AgeChanged -> _state.update { it.copy(age = event.age) }
            is BasicProfileEvent.GenderChanged -> _state.update { it.copy(gender = event.gender) }
            is BasicProfileEvent.HeightChanged -> _state.update { it.copy(height = event.height) }
            is BasicProfileEvent.WeightChanged -> _state.update { it.copy(weight = event.weight) }
            is BasicProfileEvent.BloodGroupChanged -> _state.update { it.copy(bloodGroup = event.bloodGroup) }
            is BasicProfileEvent.ContinueClicked -> saveProfile()
        }
    }

    private fun saveProfile() {
        if (!_state.value.isContinueEnabled) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(1500)
            _state.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}