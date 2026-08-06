package com.rahul.symptoscan.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.rahul.symptoscan.ui.theme.BlueAccent
import com.rahul.symptoscan.ui.theme.BluePrimary

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundTransition")
    
    // Slow floating effect for the background
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatingEffect"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BluePrimary, BlueAccent)
                )
            )
    ) {
        // Subtle medical wave patterns (Circles)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 400f + offsetAnim,
                center = Offset(canvasWidth * 0.8f, canvasHeight * 0.2f),
                style = Stroke(width = 2f)
            )
            
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = 600f - (offsetAnim * 0.5f),
                center = Offset(canvasWidth * 0.1f, canvasHeight * 0.7f),
                style = Stroke(width = 1.5f)
            )
            
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = 300f + (offsetAnim * 1.5f),
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.5f),
                style = Stroke(width = 1f)
            )
        }
        
        content()
    }
}