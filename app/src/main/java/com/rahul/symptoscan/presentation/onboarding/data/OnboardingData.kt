package com.rahul.symptoscan.presentation.onboarding.data

import androidx.compose.ui.graphics.Color
import com.rahul.symptoscan.presentation.onboarding.model.OnboardingPage

object OnboardingData {
    val pages = listOf(
        OnboardingPage(
            illustration = 0, // Placeholder
            title = "AI Medical Assessment",
            description = "Describe your symptoms naturally and receive AI-powered health guidance based on trusted medical knowledge.",
            themeColor = Color(0xFF2563EB)
        ),
        OnboardingPage(
            illustration = 0, // Placeholder
            title = "Understand Your Health Risks",
            description = "Our AI analyzes your symptoms and health profile to identify possible common conditions and explain them clearly.",
            themeColor = Color(0xFF22C55E)
        ),
        OnboardingPage(
            illustration = 0, // Placeholder
            title = "Assess • Understand • Act",
            description = "Receive structured health guidance, recognize warning signs, and know when to seek professional medical care.",
            themeColor = Color(0xFFF59E0B)
        )
    )
}
