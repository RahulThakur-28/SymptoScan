package com.rahul.symptoscan.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.createSavedStateHandle
import com.rahul.symptoscan.core.di.Injection
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
import kotlinx.coroutines.launch

@Composable
fun MainTabsScreen(
    initialTabRoute: String = "home",
    initialConversationId: String? = null,
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

    // Shared ViewModels (scoped to this screen)
    // We provide factories for all to avoid zero-arg constructor crashes
    val homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HomeViewModel() }
        }
    )
    val historyViewModel: HistoryViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HistoryViewModel() }
        }
    )
    val assessmentViewModel: AssessmentViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AssessmentViewModel() }
        }
    )
    val aiViewModel: HealthAssistantViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HealthAssistantViewModel(
                    repository = Injection.healthAssistantRepository,
                    authRepository = Injection.authRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ProfileViewModel() }
        }
    )

    // Update pager if route changes externally
    LaunchedEffect(initialTabRoute, initialConversationId) {
        val index = mainTabs.indexOf(initialTabRoute)
        if (index != -1 && index != pagerState.currentPage) {
            pagerState.scrollToPage(index)
        }
        
        if (initialTabRoute == "ai" && initialConversationId != null) {
            aiViewModel.openConversation(initialConversationId)
        }
    }

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
