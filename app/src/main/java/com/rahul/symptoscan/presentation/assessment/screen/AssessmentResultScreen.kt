package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.domain.model.AssessmentCondition
import com.rahul.symptoscan.domain.model.Recommendation
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.ui.components.MedicalDisclaimer
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentResultScreen(
    onNavigateBack: () -> Unit,
    onViewFullReport: () -> Unit,
    onAskAI: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val result = uiState.result ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assessment Complete", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DeepBlue, BluePrimary)
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RiskScoreCircular(score = result.riskScore, level = result.riskLevel)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "${result.riskLevel} Assessment",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Based on ${result.symptomCount} symptoms • ${result.date}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // AI Explanation
                SectionTitle(title = "AI Explanation")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.SmartToy,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = result.aiExplanation,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = TextDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Possible Conditions
                SectionTitle(title = "Possible Conditions")
                result.conditions.forEach { condition ->
                    ConditionCard(condition = condition)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recommendations
                SectionTitle(title = "Recommendations")
                RecommendationGrid(recommendations = result.recommendations)

                Spacer(modifier = Modifier.height(24.dp))

                // Specialist Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BluePrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.PersonSearch, contentDescription = null, tint = BluePrimary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Recommended Specialist",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(result.specialist.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(result.specialist.description, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                MedicalDisclaimer()

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButton(text = "View Full Report", onClick = onViewFullReport)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onAskAI,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Ask AI")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Download PDF")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RiskScoreCircular(score: Int, level: String) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(120.dp),
            color = Color.White.copy(alpha = 0.2f),
            strokeWidth = 10.dp,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.size(120.dp),
            color = Color.White,
            strokeWidth = 10.dp,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = score.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = level, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun ConditionCard(condition: AssessmentCondition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(condition.icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(condition.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            condition.severity,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${condition.confidence}% Confidence", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { condition.confidence / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = BluePrimary,
                    trackColor = Color.LightGray.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun RecommendationGrid(recommendations: List<Recommendation>) {
    androidx.compose.foundation.layout.FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        recommendations.forEach { recommendation ->
            Card(
                modifier = Modifier.weight(1f).height(100.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(recommendation.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(recommendation.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
        }
    }
}

