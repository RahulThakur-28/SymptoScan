package com.rahul.symptoscan.presentation.onboarding.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.common.AppDescription
import com.rahul.symptoscan.common.AppTitle
import com.rahul.symptoscan.common.OnboardingIllustration
import com.rahul.symptoscan.presentation.onboarding.model.OnboardingPage

@Composable
fun OnboardingCard(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OnboardingIllustration(color = page.themeColor)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        AppTitle(
            text = page.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AppDescription(
            text = page.description,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
