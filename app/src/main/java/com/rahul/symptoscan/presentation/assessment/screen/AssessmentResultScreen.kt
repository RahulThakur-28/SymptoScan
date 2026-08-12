package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.ui.theme.*

@Composable
fun AssessmentResultScreen(
    onNavigateBack: () -> Unit,
    onViewFullReport: () -> Unit,
    onAskAI: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val result = uiState.result
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Urgency Header
                UrgencyBadge(urgency = result.urgencyLevel ?: "Moderate")

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Assessment Summary",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = result.summary ?: "",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                ResultSection(title = "Possible Explanations", items = result.possibleCauses ?: emptyList(), iconColor = BluePrimary)

                Spacer(modifier = Modifier.height(24.dp))

                ResultSection(title = "Recommendations", items = result.recommendations ?: emptyList(), iconColor = SuccessGreen)

                Spacer(modifier = Modifier.height(24.dp))

                if (!result.warningSigns.isNullOrEmpty()) {
                    ResultSection(title = "Warning Signs", items = result.warningSigns, iconColor = DangerRed)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Disclaimer
                Text(
                    text = result.disclaimer ?: "This information is for general educational purposes and is not a medical diagnosis.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Return to Home", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onAskAI,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary)
                ) {
                    Text("Discuss with AI Assistant", color = BluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun UrgencyBadge(urgency: String) {
    val color = when (urgency.lowercase()) {
        "emergency" -> DangerRed
        "urgent" -> Color(0xFFF59E0B) // Using a manual orange color
        "routine" -> SuccessGreen
        else -> BluePrimary
    }

    val label = when (urgency.lowercase()) {
        "emergency" -> "EMERGENCY CARE"
        "urgent" -> "URGENT CARE"
        "routine" -> "ROUTINE CARE"
        else -> "SELF CARE"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ResultSection(title: String, items: List<String>, iconColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(12.dp))
        items.forEach { item ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(
                    imageVector = if (iconColor == DangerRed) Icons.Default.Warning else Icons.Default.Check,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = item, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
            }
        }
    }
}
