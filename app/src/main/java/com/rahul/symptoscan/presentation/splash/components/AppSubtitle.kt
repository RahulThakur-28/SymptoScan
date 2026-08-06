package com.rahul.symptoscan.presentation.splash.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.rahul.symptoscan.ui.theme.Dimens

/**
 * Animated Tagline for the healthcare application.
 */
@Composable
fun AppSubtitle(
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    Text(
        text = "AI-Powered Health Assessment",
        color = Color.White.copy(alpha = 0.7f * alpha),
        fontSize = Dimens.SubtitleSize,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}