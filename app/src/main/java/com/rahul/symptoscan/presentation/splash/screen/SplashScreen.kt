package com.rahul.symptoscan.presentation.splash.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.splash.components.*
import com.rahul.symptoscan.presentation.splash.viewmodel.SplashNavigationState
import com.rahul.symptoscan.presentation.splash.viewmodel.SplashViewModel
import com.rahul.symptoscan.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium SplashScreen for SymptoScan with orchestrated Material Motion.
 */
@Composable
fun SplashScreen(
    onNavigate: (SplashNavigationState) -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val navigationState by viewModel.navigationState.collectAsState()
    
    // Animation States
    val bgAlpha = remember { Animatable(0f) }
    val waveVisible = remember { mutableStateOf(false) }
    val logoScale = remember { Animatable(0.8f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoGlow = remember { Animatable(0f) } // Float representing dp
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(10f) } // Float representing dp
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 0 ms: Background Fade In
        launch {
            bgAlpha.animateTo(1f, animationSpec = tween(1000))
        }
        
        // 200 ms: Background wave animation starts
        delay(200)
        waveVisible.value = true
        
        // 300 ms: Logo Fade + Scale
        delay(100)
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(700))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = overshootTween()
            )
        }
        
        // 900 ms: Logo Glow
        delay(600)
        launch {
            logoGlow.animateTo(24f, animationSpec = tween(800))
        }
        
        // 1100 ms: App Name Fade In
        delay(200)
        launch {
            titleAlpha.animateTo(1f, animationSpec = tween(800))
        }
        launch {
            titleOffset.animateTo(0f, animationSpec = tween(800))
        }
        
        // 1400 ms: Tagline Fade In
        delay(300)
        launch {
            taglineAlpha.animateTo(1f, animationSpec = tween(800))
        }
    }

    // React to ViewModel navigation state
    LaunchedEffect(navigationState) {
        if (navigationState !is SplashNavigationState.Idle) {
            onNavigate(navigationState)
        }
    }

    GradientBackground(modifier = Modifier.fillMaxSize().alpha(bgAlpha.value)) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Animation (Infinite pulses)
            AnimatedWaveBackground(
                modifier = Modifier.fillMaxSize(),
                isVisible = waveVisible.value
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.PaddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                SplashLogo(
                    alpha = logoAlpha.value,
                    scale = logoScale.value,
                    glowRadius = logoGlow.value.dp
                )

                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

                AppTitle(
                    alpha = titleAlpha.value,
                    offsetY = titleOffset.value.dp
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                AppSubtitle(alpha = taglineAlpha.value)

                Spacer(modifier = Modifier.weight(1.2f))
            }
        }
    }
}

/**
 * Custom Overshoot Easing for Premium Feel
 */
private fun overshootTween() = tween<Float>(
    durationMillis = 700,
    easing = Easing { fraction ->
        val tension = 2f
        val t = fraction - 1f
        t * t * ((tension + 1) * t + tension) + 1f
    }
)