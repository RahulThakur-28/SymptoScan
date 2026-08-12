package com.rahul.symptoscan.presentation.home.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.auth.common.AuthViewModel
import com.rahul.symptoscan.presentation.home.component.*
import com.rahul.symptoscan.presentation.home.viewmodel.HomeViewModel
import com.rahul.symptoscan.ui.theme.BackgroundLight



@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        authViewModel.navigationEvent.collect { event ->
            if (event is AuthViewModel.AuthNavigation.NavigateToLogin) {
                onLogout()
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            HomeBottomNavigation(
                currentRoute = "home",
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading && uiState.userName.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = com.rahul.symptoscan.ui.theme.BluePrimary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        HomeHeader(
                            userName = uiState.userName,
                            initials = uiState.initials,
                            notificationCount = uiState.notificationCount,
                            onProfileClick = { onNavigate("profile") },
                            onNotificationClick = { onNavigate("notifications") }
                        )
                        
                        Column {
                            Spacer(modifier = Modifier.height(130.dp))
                            HealthScoreCard(
                                score = uiState.healthScore,
                                status = uiState.healthStatus,
                                bmi = uiState.bmi,
                                lastCheck = uiState.lastCheck,
                                assessmentCount = uiState.assessmentCount
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!uiState.isProfileComplete) {
                        CompleteProfilePrompt(onClick = { onNavigate(com.rahul.symptoscan.navigation.Screen.CompleteProfile.route) })
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    QuickActionsGrid(
                        onNewAssessment = { onNavigate("assess") },
                        onViewHistory = { onNavigate("history") },
                        onAskAI = { onNavigate("ai") },
                        onEmergency = { onNavigate("emergency") }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    DailyHealthTipCard(tip = uiState.dailyHealthTip)

                    Spacer(modifier = Modifier.height(24.dp))

                    RecentAssessmentsSection(
                        assessments = uiState.recentAssessments,
                        onSeeAllClick = { onNavigate("history") },
                        onAssessmentClick = { id -> onNavigate("assessment_details/$id") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    EmergencySosCard(onClick = { onNavigate("emergency") })

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
