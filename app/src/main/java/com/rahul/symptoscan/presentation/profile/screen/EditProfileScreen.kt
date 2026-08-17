package com.rahul.symptoscan.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.profile.viewmodel.EditProfileViewModel
import com.rahul.symptoscan.ui.components.AppDropdown
import com.rahul.symptoscan.ui.components.AppTextField
import com.rahul.symptoscan.ui.components.DatePickerField
import com.rahul.symptoscan.ui.components.NumberTextField
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.Dimens
import com.rahul.symptoscan.ui.theme.SymptoScanTheme
import com.rahul.symptoscan.ui.theme.TextDark
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = androidx.compose.ui.platform.LocalView.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    SideEffect {
        val window = (view.context as android.app.Activity).window
        androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Edit Profile", 
                            fontSize = 20.sp, 
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
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.PaddingLarge)
            ) {
                AppTextField(
                    value = profile!!.fullName,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = "Full Name",
                    placeholder = "Enter your full name"
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                
                DatePickerField(
                    label = "Date of Birth",
                    value = profile!!.dob ?: "",
                    onDateSelected = { viewModel.onDobChange(it) },
                    placeholder = "Select your birth date"
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                
                AppDropdown(
                    value = profile!!.gender ?: "",
                    onValueChange = { viewModel.onGenderChange(it) },
                    label = "Gender",
                    options = listOf("Male", "Female", "Other"),
                    placeholder = "Select Gender"
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                
                AppDropdown(
                    value = profile!!.bloodGroup ?: "",
                    onValueChange = { viewModel.onBloodGroupChange(it) },
                    label = "Blood Group",
                    options = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
                    placeholder = "Select Blood Group"
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    NumberTextField(
                        value = profile!!.height?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "",
                        onValueChange = { viewModel.onHeightChange(it) },
                        label = "Height",
                        placeholder = "0",
                        suffix = "cm",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(Dimens.PaddingMedium))
                    NumberTextField(
                        value = profile!!.weight?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "",
                        onValueChange = { viewModel.onWeightChange(it) },
                        label = "Weight",
                        placeholder = "0",
                        suffix = "kg",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

                AppTextField(
                    value = profile!!.allergies ?: "",
                    onValueChange = { viewModel.onAllergiesChange(it) },
                    label = "Allergies",
                    placeholder = "e.g. Peanuts, Penicillin",
                    singleLine = false,
                    minLines = 3
                )

                if (uiState is EditProfileViewModel.EditProfileUiState.Error) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = (uiState as EditProfileViewModel.EditProfileUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                PrimaryButton(
                    text = "Update Profile",
                    onClick = { viewModel.saveProfile(onNavigateBack) },
                    isLoading = uiState is EditProfileViewModel.EditProfileUiState.Loading,
                    enabled = uiState !is EditProfileViewModel.EditProfileUiState.Loading
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    SymptoScanTheme {
        EditProfileScreen(onNavigateBack = {})
    }
}
