package com.rahul.symptoscan.presentation.onboarding.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.ui.theme.BluePrimary

@Composable
fun PaginationIndicator(
    pageSize: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = BluePrimary,
    inactiveColor: Color = Color.LightGray.copy(alpha = 0.5f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageSize) { page ->
            val isSelected = currentPage == page
            val width = animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp, label = "Width")
            val color = animateColorAsState(targetValue = if (isSelected) activeColor else inactiveColor, label = "Color")

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width.value)
                    .clip(CircleShape)
                    .background(color.value)
            )
        }
    }
}