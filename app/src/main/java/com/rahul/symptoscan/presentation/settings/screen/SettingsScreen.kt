package com.rahul.symptoscan.presentation.settings.screen

import androidx.compose.foundation.clickable
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
import com.rahul.symptoscan.ui.theme.ThemeMode
import com.rahul.symptoscan.presentation.settings.viewmodel.SettingsViewModel
import com.rahul.symptoscan.ui.theme.SymptoScanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(shadowElevation = 2.dp) {
                TopAppBar(
                    title = { Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
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
                SettingsItem(
                    title = "Theme",
                    subtitle = when(uiState.themeMode) {
                        ThemeMode.System -> "System Default"
                        ThemeMode.Light -> "Light"
                        ThemeMode.Dark -> "Dark"
                    },
                    icon = Icons.Outlined.DarkMode,
                    onClick = { showThemeDialog = true }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp), 
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
                SettingsItem(
                    title = "Language",
                    subtitle = uiState.currentLanguage,
                    icon = Icons.Outlined.Language,
                    onClick = { onNavigate("language") }
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
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp), 
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
                SettingsSwitchItem(
                    title = "Email Reports",
                    subtitle = "Weekly health digest",
                    icon = Icons.Outlined.Email,
                    checked = uiState.emailReports,
                    onCheckedChange = { viewModel.toggleEmailReports(it) }
                )
            }

            SettingsSection(title = "PRIVACY & SECURITY") {
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = { onNavigate("privacy_policy") }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp), 
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
                SettingsItem(
                    title = "Terms of Service",
                    icon = Icons.Outlined.Description,
                    onClick = { onNavigate("terms_of_service") }
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
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp), 
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
                SettingsItem(
                    title = "Changelog",
                    icon = Icons.Outlined.History,
                    onClick = { onNavigate("changelog") }
                )
            }

            SettingsSection(title = "ACCOUNT") {
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

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    ThemeOption(
                        label = "System Default",
                        selected = uiState.themeMode == com.rahul.symptoscan.ui.theme.ThemeMode.System,
                        onClick = {
                            viewModel.setThemeMode(com.rahul.symptoscan.ui.theme.ThemeMode.System)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        label = "Light",
                        selected = uiState.themeMode == com.rahul.symptoscan.ui.theme.ThemeMode.Light,
                        onClick = {
                            viewModel.setThemeMode(com.rahul.symptoscan.ui.theme.ThemeMode.Light)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        label = "Dark",
                        selected = uiState.themeMode == com.rahul.symptoscan.ui.theme.ThemeMode.Dark,
                        onClick = {
                            viewModel.setThemeMode(com.rahul.symptoscan.ui.theme.ThemeMode.Dark)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SymptoScanTheme {
        SettingsScreen(onNavigate = {}, onNavigateBack = {}, onAccountDeleted = {})
    }
}
