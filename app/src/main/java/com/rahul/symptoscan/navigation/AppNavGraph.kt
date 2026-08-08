package com.rahul.symptoscan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.navDeepLink
import com.rahul.symptoscan.presentation.auth.emailverification.screen.EmailVerificationScreen
import com.rahul.symptoscan.presentation.auth.forgotpassword.screen.ForgotPasswordScreen
import com.rahul.symptoscan.presentation.auth.login.screen.LoginScreen
import com.rahul.symptoscan.presentation.auth.profile.screen.BasicProfileScreen
import com.rahul.symptoscan.presentation.auth.register.screen.RegisterScreen
import com.rahul.symptoscan.presentation.auth.resetpassword.screen.ResetPasswordScreen
import androidx.navigation.compose.navigation
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.home.screen.HomeScreen
import androidx.navigation.compose.navigation
import com.rahul.symptoscan.presentation.assessment.screen.*
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.presentation.onboarding.screen.OnboardingScreen
import com.rahul.symptoscan.presentation.splash.screen.SplashScreen
import com.rahul.symptoscan.presentation.splash.viewmodel.SplashNavigationState

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    object EmailVerification : Screen("email_verification")
    object BasicProfile : Screen("basic_profile")
    object Home : Screen("home")
    object Assess : Screen("assess")
    object History : Screen("history")
    object AI : Screen("ai")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object Emergency : Screen("emergency")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        val authDeepLink = navDeepLink {
            uriPattern = "symptoscan://auth"
        }

        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigate = { state ->
                    when (state) {
                        is SplashNavigationState.NavigateToOnboarding -> {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        is SplashNavigationState.NavigateToLogin -> {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        is SplashNavigationState.NavigateToVerification -> {
                            navController.navigate(Screen.EmailVerification.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        is SplashNavigationState.NavigateToProfile -> {
                            navController.navigate(Screen.BasicProfile.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        is SplashNavigationState.NavigateToHome -> {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        else -> {}
                    }
                }
            )
        }
        
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Login.route,
            deepLinks = listOf(authDeepLink)
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onEmailNotVerified = {
                    navController.navigate(Screen.EmailVerification.route)
                },
                onNavigateToResetPassword = {
                    navController.navigate(Screen.ResetPassword.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.BasicProfile.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onGoogleLogin = { /* Placeholder */ }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ResetPassword.route) { inclusive = true }
                    }
                },
                onUpdateSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ResetPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToVerification = {
                    navController.navigate(Screen.EmailVerification.route)
                }
            )
        }

        composable(route = Screen.EmailVerification.route) {
            EmailVerificationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVerificationSuccess = {
                    navController.navigate(Screen.BasicProfile.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.BasicProfile.route) {
            BasicProfileScreen(
                onProfileComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        navigation(
            startDestination = "symptom_selection",
            route = Screen.Assess.route
        ) {
            composable(route = "symptom_selection") { entry ->
                val viewModel: AssessmentViewModel = viewModel(
                    remember(entry) { navController.getBackStackEntry(Screen.Assess.route) }
                )
                SymptomAssessmentScreen(
                    onNavigate = { navController.navigate(it) },
                    onNavigateToDetails = { navController.navigate("symptom_details") },
                    viewModel = viewModel
                )
            }
            composable(route = "symptom_details") { entry ->
                val viewModel: AssessmentViewModel = viewModel(
                    remember(entry) { navController.getBackStackEntry(Screen.Assess.route) }
                )
                SymptomDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onProceed = { navController.navigate("ai_follow_up") },
                    viewModel = viewModel
                )
            }
            composable(route = "ai_follow_up") { entry ->
                val viewModel: AssessmentViewModel = viewModel(
                    remember(entry) { navController.getBackStackEntry(Screen.Assess.route) }
                )
                AiFollowUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onComplete = { 
                        navController.navigate("assessment_result") {
                            popUpTo("symptom_selection") { inclusive = true }
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(route = "assessment_result") { entry ->
                val viewModel: AssessmentViewModel = viewModel(
                    remember(entry) { navController.getBackStackEntry(Screen.Assess.route) }
                )
                AssessmentResultScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onViewFullReport = { navController.navigate("report") },
                    onAskAI = { navController.navigate(Screen.AI.route) },
                    viewModel = viewModel
                )
            }
            composable(route = "report") { entry ->
                val viewModel: AssessmentViewModel = viewModel(
                    remember(entry) { navController.getBackStackEntry(Screen.Assess.route) }
                )
                FullReportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
        }

        composable(route = Screen.History.route) { PlaceholderScreen(name = "History") }
        composable(route = Screen.AI.route) { PlaceholderScreen(name = "AI Assistant") }
        composable(route = Screen.Profile.route) { PlaceholderScreen(name = "Profile") }
        composable(route = Screen.Notifications.route) { PlaceholderScreen(name = "Notifications") }
        composable(route = Screen.Emergency.route) { PlaceholderScreen(name = "Emergency Guidance") }
        composable(route = "assessment_details/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            PlaceholderScreen(name = "Assessment Details for $id")
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(text = "$name Screen")
    }
}
