package com.rahul.symptoscan.presentation.splash.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AppTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = "SymptoScan",
        color = Color.White,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = modifier
    )
}

@Composable
fun Tagline(
    modifier: Modifier = Modifier
) {
    Text(
        text = "AI-Powered Health Assessment",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}