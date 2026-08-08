package com.rahul.symptoscan.presentation.profile.screen

import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
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
import com.rahul.symptoscan.domain.model.UserProfile
import com.rahul.symptoscan.presentation.home.component.HomeBottomNavigation
import com.rahul.symptoscan.presentation.profile.component.AchievementItem
import com.rahul.symptoscan.presentation.profile.component.ProfileHeaderCard
import com.rahul.symptoscan.presentation.profile.component.ProfileMenuItem
import com.rahul.symptoscan.presentation.profile.viewmodel.ProfileViewModel
import com.rahul.symptoscan.ui.components.ProfileInfoRow
import com.rahul.symptoscan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            HomeBottomNavigation(
                currentRoute = "profile",
                onNavigate = onNavigate
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { onNavigate("settings") }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (uiState.user != null) {
            val user = uiState.user!!
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    ProfileHeaderCard(
                        user = user,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = "Health Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HealthSummaryCard(user = user)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Column {
                        Text(
                            text = "Achievements",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.achievements) { achievement ->
                                AchievementItem(achievement = achievement)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        ProfileMenuItem(
                            icon = Icons.Default.Edit,
                            title = "Edit Profile",
                            subtitle = "Update personal information",
                            onClick = { onNavigate("edit_profile") }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        ProfileMenuItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "3 active alerts",
                            onClick = { onNavigate("notifications") }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        ProfileMenuItem(
                            icon = Icons.Default.Security,
                            title = "Privacy & Security",
                            subtitle = "Data protection settings",
                            onClick = { onNavigate("privacy_security") }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        ProfileMenuItem(
                            icon = Icons.Default.Description,
                            title = "Medical Records",
                            subtitle = "${user.assessmentCount} assessments stored",
                            onClick = { onNavigate("history") }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        ProfileMenuItem(
                            icon = Icons.Default.Language,
                            title = "Language",
                            subtitle = "English (US)",
                            onClick = { onNavigate("language") }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            title = "Help & Support",
                            subtitle = "FAQ and contact",
                            onClick = { onNavigate("help_support") }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        ProfileMenuItem(
                            icon = Icons.Default.StarOutline,
                            title = "Rate SymptoScan",
                            subtitle = "Share your experience",
                            onClick = { onNavigate("rate_app") }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Sign Out",
                            subtitle = "Logout from your account",
                            onClick = { showLogoutDialog = true },
                            isDestructive = true
                        )
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign out?") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HealthSummaryCard(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ProfileInfoRow(icon = Icons.Default.Cake, label = "Age", value = "${user.age ?: "--"} years")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
            ProfileInfoRow(icon = Icons.Default.Bloodtype, label = "Blood Group", value = user.bloodGroup ?: "Not provided")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
            ProfileInfoRow(icon = Icons.Default.Height, label = "Height / Weight", value = "${user.height ?: "--"} cm · ${user.weight ?: "--"} kg")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
            ProfileInfoRow(
                icon = Icons.Default.MonitorWeight, 
                label = "BMI", 
                value = "${if (user.bmi != null) String.format(Locale.US, "%.1f", user.bmi) else "--"} (${user.bmiStatus})"
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
            ProfileInfoRow(icon = Icons.Default.WarningAmber, label = "Allergies", value = if (user.allergies.isEmpty()) "None" else user.allergies.joinToString(", "))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SymptoScanTheme {
        ProfileScreen(onNavigate = {}, onLogout = {})
    }
}
