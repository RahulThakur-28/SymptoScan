package com.rahul.symptoscan.presentation.healthprofile.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Emergency
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
import com.rahul.symptoscan.presentation.healthprofile.component.HealthProfileStepHeader
import com.rahul.symptoscan.presentation.healthprofile.event.HealthProfileEvent
import com.rahul.symptoscan.presentation.healthprofile.viewmodel.HealthProfileViewModel
import com.rahul.symptoscan.ui.components.*
import com.rahul.symptoscan.ui.theme.*
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.DangerRed
import com.rahul.symptoscan.ui.theme.Dimens
import com.rahul.symptoscan.ui.theme.SuccessGreen

@Composable
fun HealthProfileScreen(
    onComplete: () -> Unit,
    isBasicMode: Boolean = false,
    viewModel: HealthProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onComplete()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.onEvent(HealthProfileEvent.Back) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(Dimens.CornerRadiusMedium),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Back", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    PrimaryButton(
                        text = if (isBasicMode && state.currentStep == 2) {
                            "Save & Continue to Home"
                        } else if (state.currentStep == 4) {
                            "Complete Profile"
                        } else {
                            "Continue"
                        },
                        onClick = { 
                            if (isBasicMode && state.currentStep == 2) {
                                viewModel.onEvent(HealthProfileEvent.SaveBasic)
                            } else if (state.currentStep == 4) {
                                viewModel.onEvent(HealthProfileEvent.Complete)
                            } else {
                                viewModel.onEvent(HealthProfileEvent.Continue)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        isLoading = state.isLoading,
                        enabled = isStepValid(state)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.PaddingLarge)
        ) {
            HealthProfileStepHeader(currentStep = state.currentStep)
            
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "StepContent"
            ) { step ->
                when (step) {
                    1 -> PersonalStep(state, viewModel)
                    2 -> BodyStep(state, viewModel)
                    3 -> MedicalStep(state, viewModel)
                    4 -> EmergencyStep(state, viewModel)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PersonalStep(state: com.rahul.symptoscan.presentation.healthprofile.state.HealthProfileUiState, viewModel: HealthProfileViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        AppTextField(
            value = state.fullName,
            onValueChange = { viewModel.onEvent(HealthProfileEvent.FullNameChanged(it)) },
            label = "Full Name",
            placeholder = "Enter your full name"
        )
        
        DatePickerField(
            label = "Date of Birth",
            value = state.dob,
            onDateSelected = { viewModel.onEvent(HealthProfileEvent.DobChanged(it)) },
            placeholder = "Select your birth date"
        )
        
        AppDropdown(
            value = state.gender,
            onValueChange = { viewModel.onEvent(HealthProfileEvent.GenderChanged(it)) },
            label = "Gender",
            options = listOf("Male", "Female", "Other"),
            placeholder = "Select Gender"
        )
    }
}

@Composable
private fun BodyStep(state: com.rahul.symptoscan.presentation.healthprofile.state.HealthProfileUiState, viewModel: HealthProfileViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NumberTextField(
                value = state.height,
                onValueChange = { viewModel.onEvent(HealthProfileEvent.HeightChanged(it)) },
                label = "Height",
                placeholder = "cm",
                suffix = "cm",
                modifier = Modifier.weight(1f)
            )
            NumberTextField(
                value = state.weight,
                onValueChange = { viewModel.onEvent(HealthProfileEvent.WeightChanged(it)) },
                label = "Weight",
                placeholder = "kg",
                suffix = "kg",
                modifier = Modifier.weight(1f)
            )
        }
        
        AppDropdown(
            value = state.bloodGroup,
            onValueChange = { viewModel.onEvent(HealthProfileEvent.BloodGroupChanged(it)) },
            label = "Blood Group",
            options = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
            placeholder = "Select Blood Group"
        )
        
        if (state.bmi != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Your BMI", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%.1f", state.bmi)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Surface(
                        color = if (state.bmiStatus == "Normal") SuccessGreen.copy(alpha = 0.1f) else WarningAmber.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = state.bmiStatus,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.bmiStatus == "Normal") SuccessGreen else WarningAmber
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicalStep(state: com.rahul.symptoscan.presentation.healthprofile.state.HealthProfileUiState, viewModel: HealthProfileViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column {
            Text("Known Allergies", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val commonAllergies = listOf("Penicillin", "Aspirin", "Peanuts", "Latex", "Shellfish")
                commonAllergies.forEach { allergy ->
                    val isSelected = state.selectedAllergies.contains(allergy)
                    SelectableChip(
                        label = allergy,
                        selected = isSelected,
                        onClick = { viewModel.onEvent(HealthProfileEvent.AllergyToggled(allergy)) },
                        selectedColor = DangerRed
                    )
                }
                
                AssistChip(
                    onClick = { /* Open Dialog */ },
                    label = { Text("Add Custom") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null // Use default or customized BorderStroke
                )
            }
        }
        
        Column {
            Text("Existing Conditions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            state.conditions.forEach { condition ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(condition, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(onClick = { viewModel.onEvent(HealthProfileEvent.ConditionRemoved(condition)) }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            TextButton(
                onClick = { viewModel.onEvent(HealthProfileEvent.ConditionAdded("Sample Condition")) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add condition...")
            }
        }
        
        Column {
            Text("Current Medications", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(
                value = state.medications,
                onValueChange = { viewModel.onEvent(HealthProfileEvent.MedicationsChanged(it)) },
                label = "",
                placeholder = "e.g. Metformin 500mg daily"
            )
        }
    }
}

@Composable
private fun EmergencyStep(state: com.rahul.symptoscan.presentation.healthprofile.state.HealthProfileUiState, viewModel: HealthProfileViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Surface(
            color = DangerRed.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.2f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Emergency, contentDescription = null, tint = DangerRed)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "This information will only be used in medical emergencies.",
                    fontSize = 13.sp,
                    color = DangerRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            AppTextField(
                value = state.emergencyContactName,
                onValueChange = { viewModel.onEvent(HealthProfileEvent.EmergencyContactNameChanged(it)) },
                label = "Contact Name",
                placeholder = "e.g. James Johnson"
            )
            
            AppDropdown(
                value = state.emergencyRelationship,
                onValueChange = { viewModel.onEvent(HealthProfileEvent.EmergencyRelationshipChanged(it)) },
                label = "Relationship",
                options = listOf("Spouse", "Parent", "Child", "Sibling", "Friend", "Other"),
                placeholder = "Select Relationship"
            )
            
            AppTextField(
                value = state.emergencyPhone,
                onValueChange = { viewModel.onEvent(HealthProfileEvent.EmergencyPhoneChanged(it)) },
                label = "Phone Number",
                placeholder = "e.g. +1 (555) 123-4567",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                )
            )
        }
    }
}

private fun isStepValid(state: com.rahul.symptoscan.presentation.healthprofile.state.HealthProfileUiState): Boolean {
    return when (state.currentStep) {
        1 -> state.fullName.isNotBlank() && state.dob.isNotBlank() && state.gender.isNotBlank()
        2 -> state.height.isNotBlank() && state.weight.isNotBlank() && state.bloodGroup.isNotBlank()
        3 -> true // All optional
        4 -> state.emergencyContactName.isNotBlank() && state.emergencyRelationship.isNotBlank() && state.emergencyPhone.isNotBlank()
        else -> false
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = BluePrimary
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.CornerRadiusMedium))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(Dimens.CornerRadiusMedium)
            ),
        color = if (selected) selectedColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
