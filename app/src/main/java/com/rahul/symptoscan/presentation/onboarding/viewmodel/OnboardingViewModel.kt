package com.rahul.symptoscan.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.symptoscan.presentation.onboarding.event.OnboardingEvent
import com.rahul.symptoscan.presentation.onboarding.data.OnboardingData
import com.rahul.symptoscan.presentation.onboarding.state.OnboardingState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<OnboardingNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.NextPage -> {
                if (_state.value.isLastPage) {
                    navigateToLogin()
                } else {
                    // Logic for pager handled in UI, but we could emit an event here
                }
            }
            is OnboardingEvent.SkipOnboarding -> {
                navigateToLogin()
            }
            is OnboardingEvent.PageChanged -> {
                _state.update { 
                    it.copy(
                        currentPage = event.page,
                        isLastPage = event.page == OnboardingData.pages.size - 1
                    )
                }
            }
        }
    }

    private fun navigateToLogin() {
        viewModelScope.launch {
            _navigationEvent.emit(OnboardingNavigation.NavigateToLogin)
        }
    }

    sealed class OnboardingNavigation {
        object NavigateToLogin : OnboardingNavigation()
    }
}