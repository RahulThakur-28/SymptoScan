package com.rahul.symptoscan.presentation.splash.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.R

/**
 * Premium Splash Logo that uses the production app_logo drawable.
 * Exactly centered and sized for visual focal point.
 */
@Composable
fun SplashLogo(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    scale: Float = 1f
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "SymptoScan Logo",
        modifier = modifier
            .size(260.dp)
            .scale(scale)
            .alpha(alpha),
        contentScale = ContentScale.Fit
    )
}
