package com.rahul.symptoscan.presentation.profile.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.profile.viewmodel.EditProfileViewModel
import com.rahul.symptoscan.domain.model.UserProfile
import com.rahul.symptoscan.ui.components.AppDropdown
import com.rahul.symptoscan.ui.components.AppTextField
import com.rahul.symptoscan.ui.components.NumberTextField
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.SymptoScanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                AppTextField(
                    value = profile!!.fullName,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = "Full Name",
                    placeholder = "Enter your full name"
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                AppTextField(
                    value = profile!!.dob ?: "",
                    onValueChange = { viewModel.onDobChange(it) },
                    label = "Date of Birth (YYYY-MM-DD)",
                    placeholder = "1995-08-15"
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                AppDropdown(
                    value = profile!!.gender ?: "",
                    onValueChange = { viewModel.onGenderChange(it) },
                    label = "Gender",
                    options = listOf("Male", "Female", "Other")
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                AppDropdown(
                    value = profile!!.bloodGroup ?: "",
                    onValueChange = { viewModel.onBloodGroupChange(it) },
                    label = "Blood Group",
                    options = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    NumberTextField(
                        value = profile!!.height?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "",
                        onValueChange = { viewModel.onHeightChange(it) },
                        label = "Height",
                        placeholder = "cm",
                        suffix = "cm",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    NumberTextField(
                        value = profile!!.weight?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "",
                        onValueChange = { viewModel.onWeightChange(it) },
                        label = "Weight",
                        placeholder = "kg",
                        suffix = "kg",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = profile!!.allergies ?: "",
                    onValueChange = { viewModel.onAllergiesChange(it) },
                    label = "Allergies",
                    placeholder = "e.g. Peanuts, Penicillin"
                )

                if (uiState is EditProfileViewModel.EditProfileUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (uiState as EditProfileViewModel.EditProfileUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                PrimaryButton(
                    text = "Save Changes",
                    onClick = { viewModel.saveProfile(onNavigateBack) },
                    isLoading = uiState is EditProfileViewModel.EditProfileUiState.Loading
                )
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
