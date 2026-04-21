package com.example.rahul.symptoscan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahul.symptoscan.ui.screens.*
import com.example.rahul.symptoscan.ui.viewmodel.AuthViewModel
import com.example.rahul.symptoscan.ui.viewmodel.SymptomViewModel

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val symptomViewModel: SymptomViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }

        composable(Screen.Signup.route) {
            SignupScreen(navController, authViewModel)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController, symptomViewModel)
        }

        composable(Screen.Chat.route) {
            ChatScreen(navController, symptomViewModel)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController, symptomViewModel)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController, authViewModel)
        }
    }
}