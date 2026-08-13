package com.rahul.symptoscan.presentation.splash.screen

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.splash.components.*
import com.rahul.symptoscan.presentation.splash.viewmodel.SplashNavigationState
import com.rahul.symptoscan.presentation.splash.viewmodel.SplashUiState
import com.rahul.symptoscan.presentation.splash.viewmodel.SplashViewModel
import com.rahul.symptoscan.ui.components.GradientBackground
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
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Force light icons on the blue background (white icons)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    
    // Animation States
    val logoScale = remember { Animatable(0.92f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(16f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo Fade + Scale
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(1000, easing = LinearOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, easing = LowVelocityOvershootEasing)
            )
        }
        
        // App Title Fade In + Slide Up
        delay(500)
        launch {
            titleAlpha.animateTo(1f, animationSpec = tween(800))
        }
        launch {
            titleOffset.animateTo(0f, animationSpec = tween(800, easing = EaseOutQuart))
        }
        
        // Subtitle Fade In
        delay(300)
        launch {
            taglineAlpha.animateTo(1f, animationSpec = tween(800))
        }
    }

    // React to ViewModel navigation state
    LaunchedEffect(navigationState) {
        if (navigationState !is SplashNavigationState.Idle) {
            // Standard splash duration for premium establishing feel
            delay(2000)
            onNavigate(navigationState)
            viewModel.onNavigationHandled()
        }
    }

    GradientBackground(
        modifier = Modifier.fillMaxSize(),
        showDecorations = false
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Background Animation (6 Concentric Rings)
            // Sized at 720dp to accommodate the largest ring
            AnimatedWaveBackground(
                modifier = Modifier.size(720.dp),
                isVisible = true
            )

            // Hero Composition: Logo and Text
            // We use a Column centered in the Box. 
            // To ensure the Logo is at the EXACT visual center of the screen,
            // we add a balancing spacer at the top equal to the height of the text section below.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Balancing Spacer (Approximate height of Title + Subtitle + Spacings)
                // This ensures the logo's midpoint stays at the screen center.
                Spacer(modifier = Modifier.height(96.dp))

                SplashLogo(
                    alpha = logoAlpha.value,
                    scale = logoScale.value
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppTitle(
                    alpha = titleAlpha.value,
                    offsetY = titleOffset.value.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppSubtitle(alpha = taglineAlpha.value)
            }

            // Error display at bottom if startup fails
            if (uiState is SplashUiState.Error) {
                Text(
                    text = (uiState as SplashUiState.Error).message,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                )
            }
        }
    }
}

/**
 * Custom Overshoot Easing for Premium Feel
 */
private val LowVelocityOvershootEasing = Easing { fraction ->
    val tension = 1.5f
    val t = fraction - 1f
    t * t * ((tension + 1) * t + tension) + 1f
}
