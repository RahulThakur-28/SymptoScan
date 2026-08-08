package com.rahul.symptoscan.presentation.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.settings.component.SettingsItem
import com.rahul.symptoscan.presentation.settings.component.SettingsSection
import com.rahul.symptoscan.presentation.settings.component.SettingsSwitchItem
import com.rahul.symptoscan.presentation.settings.viewmodel.SettingsViewModel
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.SymptoScanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                .padding(bottom = 32.dp)
        ) {
            SettingsSection(title = "APPEARANCE") {
                SettingsSwitchItem(
                    title = "Dark Mode",
                    subtitle = if (uiState.isDarkMode) "Dark theme active" else "Light theme active",
                    icon = Icons.Outlined.DarkMode,
                    checked = uiState.isDarkMode,
                    onCheckedChange = viewModel::toggleDarkMode
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))
                SettingsItem(
                    title = "Language",
                    subtitle = uiState.currentLanguage,
                    icon = Icons.Outlined.Language,
                    onClick = { /* Navigate to language selection */ }
                )
            }

            SettingsSection(title = "NOTIFICATIONS") {
                SettingsSwitchItem(
                    title = "Push Notifications",
                    subtitle = "Assessment reminders, tips",
                    icon = Icons.Outlined.Notifications,
                    checked = uiState.pushNotifications,
                    onCheckedChange = { viewModel.togglePushNotifications(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))
                SettingsSwitchItem(
                    title = "Email Reports",
                    subtitle = "Weekly health digest",
                    icon = Icons.Outlined.Email,
                    checked = uiState.emailReports,
                    onCheckedChange = { viewModel.toggleEmailReports(it) }
                )
            }

            SettingsSection(title = "PRIVACY & SECURITY") {
                SettingsSwitchItem(
                    title = "Biometric Login",
                    subtitle = "Face ID / Fingerprint",
                    icon = Icons.Outlined.Fingerprint,
                    checked = uiState.biometricEnabled,
                    onCheckedChange = { viewModel.toggleBiometric(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = { /* Open URL */ }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))
                SettingsItem(
                    title = "Terms of Service",
                    icon = Icons.Outlined.Description,
                    onClick = { /* Open URL */ }
                )
            }

            SettingsSection(title = "ABOUT") {
                SettingsItem(
                    title = "Version",
                    subtitle = uiState.appVersion,
                    icon = Icons.Outlined.Info,
                    onClick = { },
                    showChevron = false
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))
                SettingsItem(
                    title = "Changelog",
                    icon = Icons.Outlined.History,
                    onClick = { }
                )
            }

            SettingsSection(title = "DATA MANAGEMENT") {
                SettingsItem(
                    title = "Export My Data",
                    subtitle = "Download all health records",
                    icon = Icons.Outlined.Download,
                    onClick = { }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))
                SettingsItem(
                    title = "Delete Account",
                    subtitle = "Permanently remove all data",
                    icon = Icons.Outlined.DeleteForever,
                    onClick = { viewModel.onDeleteAccountClicked() },
                    isDestructive = true
                )
            }
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteDialog() },
            title = { Text("Delete your account?") },
            text = { Text("This will permanently delete your account, health profile, assessments, reports, AI conversations, and associated data. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.onDismissDeleteDialog()
                        viewModel.confirmDeleteAccount(onAccountDeleted)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SymptoScanTheme {
        SettingsScreen(onNavigateBack = {}, onAccountDeleted = {})
    }
}
