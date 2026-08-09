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

import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp

/**
 * Animated medical wave circles for the splash background.
 * redesigned proportionally around a larger 260dp logo.
 */
@Composable
fun AnimatedWaveBackground(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransition")
    
    // Extremely subtle scale pulse
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveScale"
    )

    // Extremely subtle opacity pulse
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveAlpha"
    )

    Canvas(modifier = modifier) {
        if (!isVisible) return@Canvas
        
        val center = Offset(size.width / 2, size.height / 2)
        
        // Final ring sizes for 260dp logo
        val ringSizes = listOf(300.dp, 360.dp, 430.dp, 520.dp, 620.dp, 720.dp)
        // High opacity for inner, very low for outer
        val baseOpacities = listOf(0.08f, 0.06f, 0.04f, 0.03f, 0.02f, 0.01f)

        ringSizes.forEachIndexed { index, sizeDp ->
            val radius = (sizeDp.toPx() / 2f) * scale
            val opacity = baseOpacities[index] * alphaPulse
            
            drawCircle(
                color = Color.White.copy(alpha = opacity),
                radius = radius,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
    }
}