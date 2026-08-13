package com.rahul.symptoscan.presentation.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.presentation.settings.component.SettingsItem
import com.rahul.symptoscan.presentation.settings.component.SettingsSection
import com.rahul.symptoscan.ui.theme.BackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
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
            SettingsSection(title = "LEGAL") {
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "How we protect your data",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = onNavigateToPrivacyPolicy
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp), 
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
                SettingsItem(
                    title = "Terms of Service",
                    subtitle = "Usage agreement",
                    icon = Icons.Outlined.Description,
                    onClick = onNavigateToTerms
                )
            }
            
            SettingsSection(title = "DATA PROTECTION") {
                SettingsItem(
                    title = "AI Data Usage",
                    subtitle = "Your reports are used to train AI",
                    icon = Icons.Outlined.Security,
                    onClick = { },
                    showChevron = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "SymptoScan uses end-to-end encryption for your sensitive health data stored in Supabase. We do not share your personal identification with third parties.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
