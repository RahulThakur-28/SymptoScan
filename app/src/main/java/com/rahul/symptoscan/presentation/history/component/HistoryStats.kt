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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "Total", 
                value = total, 
                color = BluePrimary.copy(alpha = 0.08f), 
                textColor = BluePrimary, 
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Low", 
                value = low, 
                color = SuccessGreen.copy(alpha = 0.08f), 
                textColor = SuccessGreen, 
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "Moderate", 
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
            .background(color, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
        }
    }
}
