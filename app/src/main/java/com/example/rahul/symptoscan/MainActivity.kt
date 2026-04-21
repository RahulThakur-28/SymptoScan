package com.example.rahul.symptoscan
import com.example.rahul.symptoscan.ui.navigation.AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.rahul.symptoscan.ui.theme.SymptoScanTheme
import com.example.rahul.symptoscan.ui.viewmodel.AuthViewModel
import com.example.rahul.symptoscan.ui.viewmodel.SymptomViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SymptoScanTheme {

                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val symptomViewModel: SymptomViewModel = viewModel()

                AppNavigation()
            }
        }
    }
}