package com.rahul.symptoscan

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.navigation.AppNavGraph
import com.rahul.symptoscan.ui.theme.SymptoScanTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val preferenceManager = Injection.preferenceManager
        
        setContent {
            val themeModeString by preferenceManager.themeMode.collectAsState()
            val themeMode = try {
                com.rahul.symptoscan.ui.theme.ThemeMode.valueOf(themeModeString)
            } catch (e: Exception) {
                com.rahul.symptoscan.ui.theme.ThemeMode.System
            }
            
            SymptoScanTheme(themeMode = themeMode) {
                navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (::navController.isInitialized) {
            navController.handleDeepLink(intent)
        }
    }
}
