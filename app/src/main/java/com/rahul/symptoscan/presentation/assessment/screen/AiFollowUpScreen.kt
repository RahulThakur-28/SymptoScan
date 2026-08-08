package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.domain.model.QuestionType
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.CyanBlue
import com.rahul.symptoscan.ui.theme.DeepBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFollowUpScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentQuestion = uiState.currentQuestion

    LaunchedEffect(uiState.result) {
        if (uiState.result != null) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(DeepBlue, BluePrimary, CyanBlue)
                        )
                    )
                    .padding(top = 48.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "AI Follow-up",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Question ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.AutoMirrored.Rounded.Help, contentDescription = "Help", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BluePrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "AI is analyzing your symptoms...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        } else if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BluePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SmartToy,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = currentQuestion.question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (currentQuestion.type == QuestionType.CHOICE) {
                    currentQuestion.options.forEach { option ->
                        val isSelected = uiState.answers[currentQuestion.id] == option
                        QuestionOptionCard(
                            text = option,
                            isSelected = isSelected,
                            onClick = { viewModel.onAnswerSelect(currentQuestion.id, option) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    ScaleSelector(
                        value = uiState.answers[currentQuestion.id]?.toIntOrNull() ?: 0,
                        onValueChange = { viewModel.onAnswerSelect(currentQuestion.id, it.toString()) }
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                PrimaryButton(
                    text = if (uiState.currentQuestionIndex == uiState.questions.size - 1) "Analyze Symptoms" else "Next Question",
                    onClick = { viewModel.onNextQuestion() },
                    enabled = uiState.answers.containsKey(currentQuestion.id)
                )
            }
        }
    }
}

@Composable
private fun QuestionOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BluePrimary.copy(alpha = 0.05f) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BluePrimary else Color.LightGray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = BluePrimary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) BluePrimary else Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun ScaleSelector(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..10).forEach { i ->
                val isSelected = value == i
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BluePrimary else Color(0xFFF1F5F9))
                        .clickable { onValueChange(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i.toString(),
                        color = if (isSelected) Color.White else Color(0xFF1E293B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Not at all", fontSize = 12.sp, color = Color.Gray)
            Text("Completely", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
