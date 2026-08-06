package com.rahul.symptoscan.presentation.splash.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.ui.theme.Dimens

/**
 * Premium Splash Logo with glass-morphism and AI circuit patterns.
 */
@Composable
fun SplashLogo(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    scale: Float = 1f,
    glowRadius: Dp = 0.dp
) {
    Box(
        modifier = modifier
            .size(Dimens.LogoSize)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Soft blue glow behind the logo (Animated)
        if (glowRadius > 0.dp) {
            Box(
                modifier = Modifier
                    .size(Dimens.LogoSize * 1.2f)
                    .blur(glowRadius)
                    .background(Color.White.copy(alpha = 0.3f * alpha))
            )
        }

        // Glass Card Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(Dimens.CornerRadiusLarge))
                .background(Color.White.copy(alpha = 0.15f * alpha))
                .padding(1.dp)
                .background(Color.White.copy(alpha = 0.1f * alpha)),
            contentAlignment = Alignment.Center
        ) {
            // AI-inspired circuit pattern
            CircuitPattern(alpha = alpha * 0.4f)

            // Medical "+" Icon
            MedicalPlusIcon(alpha = alpha)
        }
    }
}

@Composable
private fun MedicalPlusIcon(alpha: Float) {
    Canvas(modifier = Modifier.size(Dimens.IconSizeMedium)) {
        val strokeWidth = 8.dp.toPx()
        val length = size.width
        
        // Horizontal Line
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(0f, size.height / 2),
            end = Offset(length, size.height / 2),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Vertical Line
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun CircuitPattern(alpha: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Minimal circuit lines
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(w * 0.15f, h * 0.2f),
            end = Offset(w * 0.35f, h * 0.2f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(w * 0.15f, h * 0.2f),
            end = Offset(w * 0.15f, h * 0.4f),
            strokeWidth = 1.5f
        )
        
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 3f,
            center = Offset(w * 0.35f, h * 0.2f)
        )
    }
}