package com.rahul.symptoscan.presentation.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.settings.component.SettingsSection
import com.rahul.symptoscan.presentation.settings.component.SettingsSwitchItem
import com.rahul.symptoscan.presentation.settings.viewmodel.SettingsViewModel
import com.rahul.symptoscan.ui.theme.BackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            SettingsSection(title = "PREFERENCES") {
                SettingsSwitchItem(
                    title = "Push Notifications",
                    subtitle = "Assessment reminders and health tips",
                    icon = Icons.Outlined.Notifications,
                    checked = uiState.pushNotifications,
                    onCheckedChange = viewModel::togglePushNotifications
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp), 
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
                SettingsSwitchItem(
                    title = "Email Reports",
                    subtitle = "Weekly health digest and insights",
                    icon = Icons.Outlined.Email,
                    checked = uiState.emailReports,
                    onCheckedChange = viewModel::toggleEmailReports
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Note: These preferences control how SymptoScan communicates with you. You can change these at any time.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
