package com.rahul.symptoscan.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.ui.theme.Dimens

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = Dimens.LogoSize,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    iconColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(Dimens.CornerRadiusLarge))
            .background(backgroundColor)
            .padding(2.dp)
            .background(backgroundColor.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        // Circuit Pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            
            drawLine(
                color = iconColor.copy(alpha = 0.3f),
                start = Offset(0f, h * 0.3f),
                end = Offset(w * 0.4f, h * 0.3f),
                strokeWidth = 2f
            )
            drawLine(
                color = iconColor.copy(alpha = 0.3f),
                start = Offset(w * 0.6f, h * 0.7f),
                end = Offset(w, h * 0.7f),
                strokeWidth = 2f
            )
        }

        // Medical "+" Symbol
        Canvas(modifier = Modifier.size(size * 0.4f)) {
            val strokeWidth = (size.toPx() * 0.06f)
            val length = this.size.width
            
            // Horizontal
            drawLine(
                color = iconColor,
                start = Offset(0f, this.size.height / 2),
                end = Offset(length, this.size.height / 2),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Vertical
            drawLine(
                color = iconColor,
                start = Offset(this.size.width / 2, 0f),
                end = Offset(this.size.width / 2, this.size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}