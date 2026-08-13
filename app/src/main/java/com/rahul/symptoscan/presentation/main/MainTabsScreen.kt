package com.rahul.symptoscan.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rahul.symptoscan.presentation.ai.screen.HealthAssistantScreen
import com.rahul.symptoscan.presentation.ai.viewmodel.HealthAssistantViewModel
import com.rahul.symptoscan.presentation.assessment.screen.SymptomAssessmentScreen
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.presentation.history.screen.HistoryScreen
import com.rahul.symptoscan.presentation.history.viewmodel.HistoryViewModel
import com.rahul.symptoscan.presentation.home.component.HomeBottomNavigation
import com.rahul.symptoscan.presentation.home.screen.HomeScreen
import com.rahul.symptoscan.presentation.home.viewmodel.HomeViewModel
import com.rahul.symptoscan.presentation.profile.screen.ProfileScreen
import com.rahul.symptoscan.presentation.profile.viewmodel.ProfileViewModel
import com.rahul.symptoscan.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun MainTabsScreen(
    initialTabRoute: String = "home",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val mainTabs = listOf("home", "assess", "history", "ai", "profile")
    val initialIndex = mainTabs.indexOf(initialTabRoute).coerceAtLeast(0)
    
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { mainTabs.size }
    )
    val scope = rememberCoroutineScope()

    // Update pager if route changes externally
    LaunchedEffect(initialTabRoute) {
        val index = mainTabs.indexOf(initialTabRoute)
        if (index != -1 && index != pagerState.currentPage) {
            pagerState.scrollToPage(index)
        }
    }

    // Shared ViewModels (scoped to this screen or used via viewModel())
    // Note: They are defined here to keep them alive while swiping
    val homeViewModel: HomeViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val assessmentViewModel: AssessmentViewModel = viewModel()
    val aiViewModel: HealthAssistantViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    Scaffold(
        bottomBar = {
            HomeBottomNavigation(
                currentRoute = mainTabs[pagerState.currentPage],
                onNavigate = { route ->
                    val index = mainTabs.indexOf(route)
                    if (index != -1) {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    } else {
                        onNavigate(route)
                    }
                }
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            beyondViewportPageCount = 1,
            userScrollEnabled = true // Enable swiping
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    onNavigate = onNavigate,
                    onLogout = onLogout,
                    homeViewModel = homeViewModel,
                    showScaffold = false
                )
                1 -> SymptomAssessmentScreen(
                    onNavigate = onNavigate,
                    onNavigateToDetails = { onNavigate("symptom_details") },
                    viewModel = assessmentViewModel,
                    showScaffold = false
                )
                2 -> HistoryScreen(
                    onNavigate = onNavigate,
                    onAssessmentClick = { id -> onNavigate("assessment_details/$id") },
                    viewModel = historyViewModel,
                    showScaffold = false
                )
                3 -> HealthAssistantScreen(
                    onNavigate = onNavigate,
                    onBackClick = { /* No back in tab */ },
                    onHistoryClick = { onNavigate(Screen.AIHistory.route) },
                    viewModel = aiViewModel,
                    showScaffold = false
                )
                4 -> ProfileScreen(
                    onNavigate = onNavigate,
                    onLogout = onLogout,
                    viewModel = profileViewModel,
                    showScaffold = false
                )
            }
        }
    }
}
