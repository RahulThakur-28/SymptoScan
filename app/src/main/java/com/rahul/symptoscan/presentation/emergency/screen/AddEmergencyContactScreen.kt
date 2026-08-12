package com.rahul.symptoscan.presentation.emergency.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.emergency.viewmodel.AddEmergencyContactViewModel
import com.rahul.symptoscan.ui.components.AppDropdown
import com.rahul.symptoscan.ui.components.AppTextField
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmergencyContactScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEmergencyContactViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadExistingContact()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contact", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Emergency Contact Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = com.rahul.symptoscan.ui.theme.TextDark
            )
            Text(
                text = "Provide information for your primary emergency contact person.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = "Full Name",
                placeholder = "e.g. James Johnson"
            )

            Spacer(modifier = Modifier.height(20.dp))

            AppTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone Number",
                placeholder = "e.g. +1 555-0123"
            )

            Spacer(modifier = Modifier.height(20.dp))

            AppDropdown(
                value = uiState.relationship,
                onValueChange = viewModel::onRelationshipChange,
                label = "Relationship",
                options = listOf("Parent", "Mother", "Father", "Sibling", "Spouse", "Friend", "Guardian", "Other")
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = uiState.error!!, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            PrimaryButton(
                text = "Save Contact",
                onClick = { viewModel.saveContact(onNavigateBack) },
                isLoading = uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Text("Cancel", color = Color.Gray)
            }
        }
    }
}
