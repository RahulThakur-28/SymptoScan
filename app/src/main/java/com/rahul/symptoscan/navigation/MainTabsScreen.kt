package com.rahul.symptoscan.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    // Exit confirmation handler
    BackHandler {
        if (pagerState.currentPage != 0) {
            scope.launch {
                pagerState.animateScrollToPage(0)
            }
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        // ... (rest of the dialog code)
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit SymptoScan?") },
            text = { Text("Are you sure you want to exit SymptoScan?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        (context as? android.app.Activity)?.finish()
                    }
                ) {
                    Text("YES")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("NO")
                }
            }
        )
    }

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

    val view = androidx.compose.ui.platform.LocalView.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    SideEffect {
        val window = (context as android.app.Activity).window
        // Home tab (index 0) has a blue header, so we want light icons.
        // Other tabs have a surface (light) background, so we want dark icons (on light theme).
        if (pagerState.currentPage == 0) {
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        } else {
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Update pager if route changes externally
    LaunchedEffect(initialTabRoute) {
        val index = mainTabs.indexOf(initialTabRoute)
        if (index != -1 && index != pagerState.currentPage) {
            pagerState.scrollToPage(index)
        }
    }

    // Sync current tab with route argument when swiping
//    LaunchedEffect(pagerState.currentPage) {
//        val currentRoute = mainTabs[pagerState.currentPage]
//        if (currentRoute != initialTabRoute) {
//            onNavigate(currentRoute)
//        }
//    }

    LaunchedEffect(initialConversationId) {
        if (initialTabRoute == "ai" && initialConversationId != null) {
            aiViewModel.openConversation(initialConversationId)
        }
    }

    val internalOnNavigate: (String) -> Unit = { route ->
        val index = mainTabs.indexOf(route)
        if (index != -1) {
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        } else {
            onNavigate(route)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            HomeBottomNavigation(
                currentRoute = mainTabs[pagerState.currentPage],
                onNavigate = internalOnNavigate
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            beyondViewportPageCount = 2,
            userScrollEnabled = true // Enable swiping
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    onNavigate = internalOnNavigate,
                    onLogout = onLogout,
                    homeViewModel = homeViewModel,
                    showScaffold = false
                )
                1 -> SymptomAssessmentScreen(
                    onNavigate = internalOnNavigate,
                    onNavigateToDetails = { internalOnNavigate("symptom_details") },
                    viewModel = assessmentViewModel,
                    showScaffold = false
                )
                2 -> HistoryScreen(
                    onNavigate = internalOnNavigate,
                    onAssessmentClick = { id ->
                        internalOnNavigate("assessment_result?id=$id")
                    },
                    viewModel = historyViewModel,
                    showScaffold = false
                )
                3 -> HealthAssistantScreen(
                    onNavigate = internalOnNavigate,
                    onBackClick = {
                        if (initialConversationId != null) {
                            internalOnNavigate("ai")
                        } else if (pagerState.currentPage != 0) {
                            internalOnNavigate("home")
                        }
                    },
                    onHistoryClick = { internalOnNavigate(Screen.AIHistory.route) },
                    viewModel = aiViewModel,
                    showScaffold = false
                )
                4 -> ProfileScreen(
                    onNavigate = internalOnNavigate,
                    onLogout = onLogout,
                    viewModel = profileViewModel,
                    showScaffold = false
                )
            }
        }
    }
}
