package com.rahul.symptoscan.presentation.splash.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.ui.components.AppLogo
import com.rahul.symptoscan.ui.theme.Dimens

/**
 * Premium Splash Logo that wraps the base AppLogo with specialized splash animations.
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

        // Base App Logo
        AppLogo(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = Color.White.copy(alpha = 0.15f * alpha),
            iconColor = Color.White.copy(alpha = alpha)
        )
    }
}
