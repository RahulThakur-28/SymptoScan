package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.assessment.component.ImagePickerSection
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
    onAddSymptoms: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val view = androidx.compose.ui.platform.LocalView.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    SideEffect {
        val window = (view.context as android.app.Activity).window
        androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                shadowElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Assessment Details", 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back", 
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = TopAppBarDefaults.windowInsets
                )
            }
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                        Text(
                            text = "Refine your symptoms",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Providing details helps our AI give more precise guidance.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                items(uiState.selectedSymptoms) { symptom ->
                    SymptomDetailItem(
                        name = symptom.name,
                        icon = symptom.icon,
                        details = uiState.symptomDetails[symptom.id] ?: SymptomDetails(),
                        onDetailsChange = { viewModel.onSymptomDetailsChange(symptom.id, it) }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = onAddSymptoms,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Symptoms", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                item {
                    AssessmentLevelInfo(
                        temperature = uiState.bodyTemperature,
                        onTemperatureChange = { viewModel.onTemperatureChange(it) },
                        notes = uiState.additionalNotes,
                        onNotesChange = { viewModel.onNotesChange(it) }
                    )
                }

                item {
                    ImagePickerSection(
                        selectedImageUri = uiState.selectedImageUri,
                        onImageSelected = { viewModel.onImageSelected(it) },
                        onRemoveImage = { viewModel.removeImage() }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Box(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
                    PrimaryButton(
                        text = if (uiState.isImageUploading) "Uploading context..." else "Generate AI Assessment",
                        onClick = { viewModel.startAssessment(onProceed) },
                        isLoading = uiState.isLoading,
                        enabled = !uiState.isImageUploading,
                    )
                }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = name, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
            
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeviceThermostat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Body Temperature", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val tempStatus = when {
                temperature < 97.7 -> "Low"
                temperature > 99.5 -> "High (Fever)"
                else -> "Normal"
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", temperature)}°F", 
                    fontSize = 28.sp, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = when(tempStatus) {
                        "Normal" -> com.rahul.symptoscan.ui.theme.SuccessGreen.copy(alpha = 0.1f)
                        else -> com.rahul.symptoscan.ui.theme.DangerRed.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tempStatus,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(tempStatus) {
                            "Normal" -> com.rahul.symptoscan.ui.theme.SuccessGreen
                            else -> com.rahul.symptoscan.ui.theme.DangerRed
                        }
                    )
                }
            }

            Slider(
                value = temperature.toFloat(),
                onValueChange = { onTemperatureChange(it.toDouble()) },
                valueRange = 91f..108f,
                steps = 170,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Additional Notes", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { 
                    Text(
                        "Describe anything additional about your symptoms, triggers, or relevant context...", 
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ) 
                },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
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
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${value.toInt()}/10", 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
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
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            label, 
            fontSize = 15.sp, 
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface, 
            modifier = Modifier.padding(bottom = 12.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelected(option) },
                    label = { 
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 1.dp
                    )
                )
            }
        }
    }
}
