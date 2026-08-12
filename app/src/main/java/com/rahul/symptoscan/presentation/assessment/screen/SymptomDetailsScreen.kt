package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.assessment.state.SymptomDetails
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SymptomDetailsScreen(
    onNavigateBack: () -> Unit,
    onProceed: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Symptom Details") },
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
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(uiState.selectedSymptoms) { symptom ->
                    SymptomDetailItem(
                        name = symptom.name,
                        icon = symptom.icon,
                        details = uiState.symptomDetails[symptom.id] ?: SymptomDetails(),
                        onDetailsChange = { viewModel.onSymptomDetailsChange(symptom.id, it) }
                    )
                }

                item {
                    AssessmentLevelInfo(
                        temperature = uiState.bodyTemperature,
                        onTemperatureChange = { viewModel.onTemperatureChange(it) },
                        notes = uiState.additionalNotes,
                        onNotesChange = { viewModel.onNotesChange(it) }
                    )
                }
            }

            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                PrimaryButton(
                    text = "Proceed to AI Assessment",
                    onClick = { viewModel.startAssessment(onProceed) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomDetailItem(
    name: String,
    icon: String,
    details: SymptomDetails,
    onDetailsChange: (SymptomDetails) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            DetailSlider(
                label = "Severity",
                value = details.severity.toFloat(),
                range = 1f..10f,
                steps = 8,
                onValueChange = { onDetailsChange(details.copy(severity = it.toInt())) }
            )

            DetailSlider(
                label = "Pain Level",
                value = details.painLevel.toFloat(),
                range = 0f..10f,
                steps = 9,
                onValueChange = { onDetailsChange(details.copy(painLevel = it.toInt())) }
            )

            DetailChips(
                label = "Duration",
                selected = details.duration,
                options = listOf("Today", "2–3 days", "1 week", "2 weeks", "1+ month"),
                onSelected = { onDetailsChange(details.copy(duration = it)) }
            )

            DetailChips(
                label = "Frequency",
                selected = details.frequency,
                options = listOf("Constant", "Intermittent", "Occasional", "Rare"),
                onSelected = { onDetailsChange(details.copy(frequency = it)) }
            )

            DetailChips(
                label = "Onset",
                selected = details.onset,
                options = listOf("Sudden", "Gradual", "Unknown"),
                onSelected = { onDetailsChange(details.copy(onset = it)) }
            )
        }
    }
}

@Composable
private fun AssessmentLevelInfo(
    temperature: Double,
    onTemperatureChange: (Double) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Body Temperature", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            
            val tempStatus = when {
                temperature < 36.5 -> "Low"
                temperature > 37.5 -> "High (Fever)"
                else -> "Normal"
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${String.format("%.1f", temperature)}°C", fontSize = 24.sp, fontWeight = FontWeight.Black, color = BluePrimary)
                Surface(
                    color = when(tempStatus) {
                        "Normal" -> Color(0xFFF0FDF4)
                        else -> Color(0xFFFEF2F2)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tempStatus,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(tempStatus) {
                            "Normal" -> Color(0xFF16A34A)
                            else -> Color(0xFFDC2626)
                        }
                    )
                }
            }

            Slider(
                value = temperature.toFloat(),
                onValueChange = { onTemperatureChange(it.toDouble()) },
                valueRange = 35f..42f,
                steps = 69 // 0.1 increments roughly
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Additional Notes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Describe anything additional about your symptoms, triggers, or relevant context...", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun DetailSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp, color = Color.Gray)
            Text("${value.toInt()}/10", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailChips(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelected(option) },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary.copy(alpha = 0.1f),
                        selectedLabelColor = BluePrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.LightGray.copy(alpha = 0.3f),
                        selectedBorderColor = BluePrimary
                    )
                )
            }
        }
    }
}
