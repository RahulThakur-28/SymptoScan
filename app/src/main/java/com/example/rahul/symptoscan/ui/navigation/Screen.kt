package com.example.rahul.symptoscan.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Chat : Screen("chat")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Result : Screen("result/{symptoms}/{result}/{riskLevel}") {
        fun createRoute(symptoms: String, result: String, riskLevel: String) = 
            "result/$symptoms/$result/$riskLevel"
    }
}