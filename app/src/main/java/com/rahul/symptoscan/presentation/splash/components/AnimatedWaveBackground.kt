package com.rahul.symptoscan.presentation.splash.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Animated medical wave circles for the splash background.
 * Extremely smooth infinite animation.
 */
@Composable
fun AnimatedWaveBackground(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransition")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveScale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveAlpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        if (!isVisible) return@Canvas
        
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.width * 0.4f
        
        // Multiple circles for a premium medical wave effect
        for (i in 1..3) {
            drawCircle(
                color = Color.White.copy(alpha = alpha / (i * 0.8f)),
                radius = (baseRadius * i * 0.5f) * scale,
                center = center,
                style = Stroke(width = 2f)
            )
        }
    }
}