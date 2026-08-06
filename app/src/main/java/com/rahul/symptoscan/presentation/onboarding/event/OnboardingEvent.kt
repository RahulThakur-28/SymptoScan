package com.rahul.symptoscan.presentation.onboarding.event

sealed class OnboardingEvent {
    object NextPage : OnboardingEvent()
    object SkipOnboarding : OnboardingEvent()
    data class PageChanged(val page: Int) : OnboardingEvent()
}