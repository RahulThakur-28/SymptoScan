package com.rahul.symptoscan.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.ui.theme.SplashGradientBottom
import com.rahul.symptoscan.ui.theme.SplashGradientTop
import com.rahul.symptoscan.ui.theme.White70


@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    viewModel: SplashViewModel = SplashViewModel() // In real app, use hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()
    val isReady by viewModel.isReady.collectAsState()

    LaunchedEffect(isReady) {
        if (isReady && destination != null) {
            onNavigate(destination!!)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SplashGradientTop, SplashGradientBottom)
                )
            )
    ) {
        // Soft Circular Waves
        CircularWaves()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SplashLogo()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "SymptoScan",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )
            
            Text(
                text = "AI-Powered Medical Assessment",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = White70,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // Bottom Section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Preparing your AI Health Assistant...",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = White70
                )
            )
        }
    }
}

@Composable
fun SplashLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.15f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Drawing a Medical Cross + Sparkle
            Canvas(modifier = Modifier.size(40.dp)) {
                val thickness = 10.dp.toPx()
                val length = size.width
                
                // Vertical bar
                drawLine(
                    color = Color.White,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = thickness
                )
                // Horizontal bar
                drawLine(
                    color = Color.White,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = thickness
                )
            }
            
            // Sparkle icon-like drawing in the corner
            Canvas(modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
                .padding(4.dp)
            ) {
                drawCircle(color = Color.White, radius = 4f)
            }
        }
    }
}

@Composable
fun CircularWaves() {
    val infiniteTransition = rememberInfiniteTransition(label = "waves")
    val wave1Radius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = wave1Radius,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.03f),
            radius = (wave1Radius + 400f) % 1000f,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
