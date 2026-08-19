package com.rahul.symptoscan.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.ui.theme.IllustrationSize

@Composable
fun OnboardingIllustration(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "illustration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(IllustrationSize)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(IllustrationSize * 0.7f)
                .background(color.copy(alpha = 0.2f), CircleShape)
        )
        
        // Placeholder for actual illustration
        Box(
            modifier = Modifier
                .size(IllustrationSize * 0.4f)
                .background(color, MaterialTheme.shapes.large)
        )
    }
}
