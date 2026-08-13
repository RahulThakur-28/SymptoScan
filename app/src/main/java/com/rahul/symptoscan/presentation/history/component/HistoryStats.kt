package com.rahul.symptoscan.presentation.history.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.ui.theme.*

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
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = "Total", 
            value = total, 
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), 
            textColor = MaterialTheme.colorScheme.primary, 
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Low", 
            value = low, 
            color = SuccessGreen.copy(alpha = 0.08f), 
            textColor = SuccessGreen, 
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Mod.", 
            value = moderate, 
            color = WarningAmber.copy(alpha = 0.08f), 
            textColor = WarningAmber, 
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "High", 
            value = high, 
            color = DangerRed.copy(alpha = 0.08f), 
            textColor = DangerRed, 
            modifier = Modifier.weight(1f)
        )
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
            .padding(vertical = 12.dp, horizontal = 4.dp),
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
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}
