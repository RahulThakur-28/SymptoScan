package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.data.remote.model.DbAssessmentResult
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentResultScreen(
    onNavigateBack: () -> Unit,
    onViewFullReport: (String) -> Unit,
    onAskAI: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val result = uiState.result
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // HERO HEADER
                item {
                    ResultHeroHeader(
                        urgency = result.urgencyLevel ?: "self_care",
                        symptomCount = uiState.selectedSymptoms.size,
                        onBack = onNavigateBack
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // AI EXPLANATION
                item {
                    AiExplanationCard(summary = result.summary ?: "")
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // POSSIBLE EXPLANATIONS
                item {
                    SectionTitle(title = "Possible Explanations")
                }
                items(result.possibleCauses ?: emptyList()) { cause ->
                    ExplanationItemCard(cause = cause)
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // RECOMMENDATIONS
                item {
                    SectionTitle(title = "Recommendations")
                }
                items(result.recommendations ?: emptyList()) { recommendation ->
                    RecommendationItemCard(recommendation = recommendation)
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // WARNING SIGNS
                if (!result.warningSigns.isNullOrEmpty()) {
                    item {
                        SectionTitle(title = "Warning Signs")
                    }
                    items(result.warningSigns) { warning ->
                        WarningSignCard(warning = warning)
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                // DISCLAIMER
                item {
                    MedicalDisclaimerCard(disclaimer = result.disclaimer ?: "")
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                // BOTTOM ACTIONS
                item {
                    BottomActionsSection(
                        onViewFullReport = { uiState.assessmentId?.let { onViewFullReport(it) } },
                        onAskAI = onAskAI,
                        onDownloadPdf = {
                             // We need FullAssessmentReport here. 
                             // For now, navigate to Full Report to download, or we can fetch here.
                             // Let's just navigate to Full Report as it's the primary way to see all details.
                             uiState.assessmentId?.let { onViewFullReport(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultHeroHeader(
    urgency: String,
    symptomCount: Int,
    onBack: () -> Unit
) {
    val riskInfo = remember(urgency) { getRiskInfo(urgency) }
    val dateStr = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepBlue, BluePrimary)
                )
            )
            .statusBarsPadding()
            .padding(bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Assessment Complete",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { /* Share Logic */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Risk Score Circular Indicator
            CircularRiskScore(score = riskInfo.score, riskLabel = riskInfo.shortLabel, color = riskInfo.color)

            Spacer(modifier = Modifier.height(24.dp))

            // Risk Level Badge
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(riskInfo.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = riskInfo.fullLabel,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata
            Text(
                text = "Based on $symptomCount symptoms • $dateStr",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CircularRiskScore(
    score: Int,
    riskLabel: String,
    color: Color
) {
    Box(contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(140.dp)) {
            // Background track
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (score / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = riskLabel,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AiExplanationCard(summary: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = BluePrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.SmartToy,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI Explanation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = summary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun ExplanationItemCard(cause: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = cause,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun RecommendationItemCard(recommendation: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = BluePrimary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("•", color = BluePrimary, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = recommendation,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun WarningSignCard(warning: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F0)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = warning,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF991B1B)
            )
        }
    }
}

@Composable
private fun MedicalDisclaimerCard(disclaimer: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = disclaimer.ifBlank { "This information is for general educational purposes only and does not constitute a medical diagnosis or treatment. Always consult a qualified healthcare professional regarding your health concerns." },
            modifier = Modifier.padding(16.dp),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BottomActionsSection(
    onViewFullReport: () -> Unit,
    onAskAI: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
    ) {
        Button(
            onClick = onViewFullReport,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text(text = "View Full Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onAskAI,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary)
            ) {
                Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Ask AI", color = BluePrimary, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onDownloadPdf,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "PDF", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class RiskInfo(
    val color: Color,
    val shortLabel: String,
    val fullLabel: String,
    val score: Int
)

private fun getRiskInfo(urgency: String): RiskInfo {
    return when (urgency.lowercase()) {
        "emergency" -> RiskInfo(DangerRed, "High Risk", "Emergency Care", 92)
        "urgent" -> RiskInfo(WarningAmber, "High Risk", "Urgent Care", 68)
        "routine" -> RiskInfo(CyanBlue, "Moderate Risk", "Routine Care", 34)
        "self_care" -> RiskInfo(SuccessGreen, "Low Risk", "Low Risk Assessment", 15)
        else -> RiskInfo(BluePrimary, "Health Check", "General Assessment", 0)
    }
}
