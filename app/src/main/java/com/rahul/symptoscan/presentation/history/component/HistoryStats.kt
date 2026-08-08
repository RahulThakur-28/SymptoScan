package com.rahul.symptoscan.presentation.history.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryStats(
    total: Int,
    low: Int,
    moderate: Int,
    high: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(label = "Total", value = total, color = Color(0xFFEFF6FF), textColor = Color(0xFF2563EB), modifier = Modifier.weight(1f))
        StatCard(label = "Low", value = low, color = Color(0xFFF0FDF4), textColor = Color(0xFF16C76B), modifier = Modifier.weight(1f))
        StatCard(label = "Moderate", value = moderate, color = Color(0xFFFFF7ED), textColor = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
        StatCard(label = "High", value = high, color = Color(0xFFFEF2F2), textColor = Color(0xFFEF4444), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}
