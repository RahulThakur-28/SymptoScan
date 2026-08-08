package com.rahul.symptoscan.presentation.healthprofile.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.ui.theme.BluePrimary

@Composable
fun HealthProfileStepHeader(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Complete Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Help us personalize your health experience",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        HealthProfileProgress(currentStep = currentStep)
    }
}

@Composable
fun HealthProfileProgress(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val steps = listOf("Personal", "Body", "Medical", "Emergency")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep
            
            StepIndicator(
                title = title,
                isActive = isActive,
                isCompleted = isCompleted,
                modifier = Modifier.weight(1f)
            )
            
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(if (isCompleted) BluePrimary else Color.LightGray.copy(alpha = 0.5f))
                        .align(Alignment.CenterVertically)
                        .offset(y = (-10).dp) // Adjust based on circle size
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(
    title: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = if (isCompleted) BluePrimary else if (isActive) BluePrimary.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
            border = if (isActive && !isCompleted) androidx.compose.foundation.BorderStroke(2.dp, BluePrimary) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = (if (title == "Personal") "1" else if (title == "Body") "2" else if (title == "Medical") "3" else "4"),
                        color = if (isActive) BluePrimary else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Color(0xFF1E293B) else Color.Gray
        )
    }
}
