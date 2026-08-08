package com.rahul.symptoscan.presentation.privacy.screen

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
import com.rahul.symptoscan.presentation.settings.component.SettingsItem
import com.rahul.symptoscan.presentation.settings.component.SettingsSection
import com.rahul.symptoscan.presentation.settings.component.SettingsSwitchItem
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit
) {
    var biometricEnabled by remember { mutableStateOf(false) }
    var cloudSyncEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            SettingsSection(title = "ACCOUNT SECURITY") {
                SettingsSwitchItem(
                    title = "Biometric Login",
                    subtitle = "Face ID / Fingerprint",
                    icon = Icons.Outlined.Fingerprint,
                    checked = biometricEnabled,
                    onCheckedChange = { biometricEnabled = it }
                )
            }

            SettingsSection(title = "DATA SECURITY") {
                SettingsSwitchItem(
                    title = "Cloud Sync",
                    subtitle = "Sync across devices",
                    icon = Icons.Outlined.CloudUpload,
                    checked = cloudSyncEnabled,
                    onCheckedChange = { cloudSyncEnabled = it }
                )
            }

            SettingsSection(title = "DATA & PRIVACY") {
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = onNavigateToPrivacyPolicy
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))
                SettingsItem(
                    title = "Terms of Service",
                    icon = Icons.Outlined.Description,
                    onClick = onNavigateToTerms
                )
            }

            SettingsSection(title = "ACCOUNT DATA") {
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
                    onClick = onNavigateToDeleteAccount,
                    isDestructive = true
                )
            }
        }
    }
}
