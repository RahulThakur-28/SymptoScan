package com.rahul.symptoscan.presentation.onboarding.state

data class OnboardingState(
    val currentPage: Int = 0,
    val isLastPage: Boolean = false
)