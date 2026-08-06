package com.rahul.symptoscan.presentation.onboarding.model

import androidx.compose.ui.graphics.Color

data class OnboardingPage(
    val title: String,
    val description: String,
    val backgroundColor: Color = Color.White
)