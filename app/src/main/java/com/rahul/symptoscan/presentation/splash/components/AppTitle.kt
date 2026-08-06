package com.rahul.symptoscan.presentation.splash.components

import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.ui.theme.Dimens

/**
 * Animated App Title with premium typography.
 */
@Composable
fun AppTitle(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    offsetY: Dp = 0.dp
) {
    Text(
        text = "SymptoScan",
        color = Color.White.copy(alpha = alpha),
        fontSize = Dimens.TitleSize,
        fontWeight = FontWeight.Bold,
        letterSpacing = Dimens.TitleLetterSpacing,
        modifier = modifier.offset(y = offsetY)
    )
}