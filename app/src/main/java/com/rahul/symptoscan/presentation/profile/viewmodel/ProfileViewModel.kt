package com.rahul.symptoscan.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.data.repository.AuthRepository
import com.rahul.symptoscan.data.repository.ProfileRepository
import com.rahul.symptoscan.presentation.profile.state.ProfileUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository = Injection.profileRepository,
    private val authRepository: AuthRepository = Injection.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        val userId = authRepository.getCurrentUser()?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                profileRepository.getUserProfile(userId),
                profileRepository.getAchievements()
            ) { profile, achievements ->
                _uiState.update { 
                    it.copy(
                        user = profile,
                        achievements = achievements,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect()
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }
}
