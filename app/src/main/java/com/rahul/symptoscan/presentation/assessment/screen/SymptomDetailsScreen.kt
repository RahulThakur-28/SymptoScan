package com.rahul.symptoscan.presentation.assessment.screen

import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDetailsScreen(
    onNavigateBack: () -> Unit,
    onProceed: () -> Unit,
    viewModel: AssessmentViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Symptom Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = uiState.selectedSymptoms.joinToString(", ") { it.name },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // SEVERITY & PAIN
            SectionHeader(title = "SEVERITY & PAIN")
            
            DetailCard {
                SeveritySlider(
                    label = "Overall Severity",
                    value = uiState.overallSeverity,
                    onValueChange = { viewModel.onSeverityChange(it) },
                    valueText = "${uiState.overallSeverity.toInt()}/10",
                    leftLabel = "Mild",
                    rightLabel = "Severe"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SeveritySlider(
                    label = "Pain Level",
                    value = uiState.painLevel,
                    onValueChange = { viewModel.onPainLevelChange(it) },
                    valueText = "${uiState.painLevel.toInt()}/10",
                    leftLabel = "No pain",
                    rightLabel = "Worst pain"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DURATION & PATTERN
            SectionHeader(title = "DURATION & PATTERN")
            
            DetailCard {
                Text("How long have you had this?", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val durations = listOf("Today", "2–3 days", "1 week", "2 weeks", "1+ month")
                    durations.forEach { duration ->
                        SelectableChip(
                            label = duration,
                            selected = uiState.selectedDuration == duration,
                            onClick = { viewModel.onDurationSelect(duration) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("Frequency", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val frequencies = listOf("Constant", "Intermittent", "Occasional", "Rare")
                    frequencies.forEach { freq ->
                        SelectableChip(
                            label = freq,
                            selected = uiState.selectedFrequency == freq,
                            onClick = { viewModel.onFrequencySelect(freq) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Onset", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val onsets = listOf("Sudden", "Gradual", "Unknown")
                    onsets.forEach { onset ->
                        SelectableChip(
                            label = onset,
                            selected = uiState.selectedOnset == onset,
                            onClick = { viewModel.onOnsetSelect(onset) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BODY TEMPERATURE
            SectionHeader(title = "BODY TEMPERATURE")
            
            DetailCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", uiState.bodyTemperature)}°C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (uiState.bodyTemperature > 37.5) "High" else "Normal",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.bodyTemperature > 37.5) Color.Red else Color(0xFF22C55E)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = uiState.bodyTemperature,
                    onValueChange = { viewModel.onTemperatureChange(it) },
                    valueRange = 35f..42f,
                    colors = SliderDefaults.colors(
                        thumbColor = BluePrimary,
                        activeTrackColor = BluePrimary,
                        inactiveTrackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("35°C", fontSize = 12.sp, color = Color.Gray)
                    Text("42°C", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ADDITIONAL NOTES
            SectionHeader(title = "ADDITIONAL NOTES")
            
            OutlinedTextField(
                value = uiState.additionalNotes,
                onValueChange = { viewModel.onNotesChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { 
                    Text(
                        "Describe anything additional about your symptoms, triggers, or relevant context...",
                        fontSize = 14.sp
                    ) 
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Proceed to AI Assessment",
                onClick = onProceed
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
private fun SeveritySlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueText: String,
    leftLabel: String,
    rightLabel: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(valueText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = BluePrimary,
                activeTrackColor = BluePrimary,
                inactiveTrackColor = Color.LightGray.copy(alpha = 0.3f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(leftLabel, fontSize = 12.sp, color = Color.Gray)
            Text(rightLabel, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (selected) BluePrimary else Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (selected) BluePrimary.copy(alpha = 0.1f) else Color(0xFFF1F5F9)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) BluePrimary else Color(0xFF64748B)
        )
    }
}

