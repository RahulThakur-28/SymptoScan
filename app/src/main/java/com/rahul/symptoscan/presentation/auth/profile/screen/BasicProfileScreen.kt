package com.rahul.symptoscan.presentation.auth.profile.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.rahul.symptoscan.presentation.auth.profile.event.BasicProfileEvent
import com.rahul.symptoscan.presentation.auth.profile.viewmodel.BasicProfileViewModel
import com.rahul.symptoscan.ui.components.AppDropdown
import com.rahul.symptoscan.ui.components.NumberTextField
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.components.ProgressHeader
import com.rahul.symptoscan.ui.theme.Dimens

@Composable
fun BasicProfileScreen(
    onProfileComplete: () -> Unit,
    viewModel: BasicProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onProfileComplete()
        }
    }

    Scaffold(containerColor = Color.White) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.PaddingExtraLarge, vertical = Dimens.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressHeader(progress = 0.5f, label = "Profile Completion")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Complete Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            
            Text(
                text = "Help us improve the accuracy of your AI assessment.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
            
            NumberTextField(
                value = state.age,
                onValueChange = { viewModel.onEvent(BasicProfileEvent.AgeChanged(it)) },
                label = "Age",
                placeholder = "25",
                suffix = "years"
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            
            AppDropdown(
                value = state.gender,
                onValueChange = { viewModel.onEvent(BasicProfileEvent.GenderChanged(it)) },
                label = "Gender",
                options = listOf("Male", "Female", "Other")
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberTextField(
                    value = state.height,
                    onValueChange = { viewModel.onEvent(BasicProfileEvent.HeightChanged(it)) },
                    label = "Height",
                    placeholder = "175",
                    suffix = "cm",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingMedium))
                NumberTextField(
                    value = state.weight,
                    onValueChange = { viewModel.onEvent(BasicProfileEvent.WeightChanged(it)) },
                    label = "Weight",
                    placeholder = "70",
                    suffix = "kg",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            
            AppDropdown(
                value = state.bloodGroup,
                onValueChange = { viewModel.onEvent(BasicProfileEvent.BloodGroupChanged(it)) },
                label = "Blood Group (Optional)",
                options = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
            
            PrimaryButton(
                text = "Continue",
                onClick = { viewModel.onEvent(BasicProfileEvent.ContinueClicked) },
                enabled = state.isContinueEnabled,
                isLoading = state.isLoading
            )
        }
    }
}
