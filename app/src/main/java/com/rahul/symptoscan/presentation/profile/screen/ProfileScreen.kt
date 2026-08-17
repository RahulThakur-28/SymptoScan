package com.rahul.symptoscan.presentation.profile.screen

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.components.ProfileInfoRow
import com.rahul.symptoscan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    showScaffold: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    SideEffect {
        val window = (view.context as android.app.Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfileData()
    }

    if (showScaffold) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                HomeBottomNavigation(
                    currentRoute = "profile",
                    onNavigate = onNavigate
                )
            },
            topBar = {
                Surface(shadowElevation = 2.dp) {
                    TopAppBar(
                        title = { 
                            Text(
                                "Profile", 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        actions = {
                            TextButton(
                                onClick = { onNavigate("settings") },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Settings", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        ) { paddingValues ->
            ProfileScreenContent(
                paddingValues = paddingValues,
                uiState = uiState,
                onRefresh = viewModel::onRefresh,
                onNavigate = onNavigate,
                onLogoutClick = { showLogoutDialog = true },
                onRetry = { viewModel.loadProfileData() }
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Profile", 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    actions = {
                        TextButton(
                            onClick = { onNavigate("settings") },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Settings", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = TopAppBarDefaults.windowInsets
                )
            }
            ProfileScreenContent(
                paddingValues = PaddingValues(0.dp),
                uiState = uiState,
                onRefresh = viewModel::onRefresh,
                onNavigate = onNavigate,
                onLogoutClick = { showLogoutDialog = true },
                onRetry = { viewModel.loadProfileData() }
            )
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
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    paddingValues: PaddingValues,
    uiState: com.rahul.symptoscan.presentation.profile.state.ProfileUiState,
    onRefresh: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = uiState.isRefreshing,
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        if (uiState.isLoading && uiState.user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null && uiState.user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Unable to load your profile.", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(
                        text = "Try Again",
                        onClick = onRetry,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
        } else if (uiState.user != null) {
            val user = uiState.user
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                item {
                    ProfileHeaderCard(
                        user = user,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                if (!user.isProfileComplete) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            onClick = { onNavigate("complete_profile") },
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Complete your health profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                                    Text("Provide more details for better AI accuracy", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = "Health Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
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
                            color = MaterialTheme.colorScheme.onBackground,
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
                        Text(
                            text = "Account Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Edit,
                            title = "Edit Profile",
                            subtitle = "Update personal information",
                            onClick = { onNavigate("edit_profile") }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ProfileMenuItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Manage alerts and preferences",
                            onClick = { onNavigate("notifications") }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ProfileMenuItem(
                            icon = Icons.Default.Security,
                            title = "Privacy & Security",
                            subtitle = "Data protection settings",
                            onClick = { onNavigate("privacy_security") }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ProfileMenuItem(
                            icon = Icons.Default.History,
                            title = "Medical Records",
                            subtitle = "View saved assessment records",
                            onClick = { onNavigate("history") }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ProfileMenuItem(
                            icon = Icons.Default.HelpOutline,
                            title = "Help & Support",
                            subtitle = "FAQ and contact",
                            onClick = { onNavigate("help_support") }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ProfileMenuItem(
                            icon = Icons.Default.StarOutline,
                            title = "Rate SymptoScan",
                            subtitle = "Share your experience",
                            onClick = { onNavigate("rate_app") }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ProfileMenuItem(
                            icon = Icons.Default.Info,
                            title = "About SymptoScan",
                            subtitle = "App information",
                            onClick = { onNavigate("about") }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Sign Out",
                            subtitle = "Logout from your account",
                            onClick = onLogoutClick,
                            isDestructive = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthSummaryCard(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ProfileInfoRow(icon = Icons.Default.Cake, label = "Age", value = if (user.age != null) "${user.age} years" else "--")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp), 
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            ProfileInfoRow(icon = Icons.Default.Bloodtype, label = "Blood Group", value = user.bloodGroup ?: "Not provided")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp), 
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            ProfileInfoRow(icon = Icons.Default.Height, label = "Height / Weight", value = "${user.height ?: "--"} cm · ${user.weight ?: "--"} kg")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp), 
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            ProfileInfoRow(
                icon = Icons.Default.MonitorWeight, 
                label = "BMI", 
                value = "${if (user.bmi != null) String.format(Locale.US, "%.1f", user.bmi) else "--"} (${user.bmiStatus})"
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp), 
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            ProfileInfoRow(icon = Icons.Default.WarningAmber, label = "Allergies", value = user.allergies ?: "None")
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
