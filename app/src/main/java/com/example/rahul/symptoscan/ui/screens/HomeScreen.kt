package com.example.rahul.symptoscan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rahul.symptoscan.ui.components.ActionCard
import com.example.rahul.symptoscan.ui.components.BottomNavigationBar
import com.example.rahul.symptoscan.ui.navigation.Screen
import com.example.rahul.symptoscan.ui.theme.*
import com.example.rahul.symptoscan.ui.viewmodel.SymptomViewModel
import com.example.rahul.symptoscan.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: SymptomViewModel
) {

    var input by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            val data = (uiState as UiState.Success).data

            navController.navigate(
                Screen.Result.createRoute(
                    input,
                    data.conditions.joinToString(", "),
                    data.riskLevel
                )
            )
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            // / HEADER
            Text(
                text = "Hi Rahul 👋",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "How are you feeling today?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(20.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(PrimaryStart, PrimaryEnd)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {

                Text(
                    text = "✨ Symptom Analysis",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))


                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = {
                        Text(
                            "Describe your symptoms...\ne.g. Fever, headache, cough",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))


                Button(
                    onClick = {
                        viewModel.analyzeSymptoms(input)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        "Analyze Symptoms →",
                        color = PrimaryStart,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ERROR
                if (uiState is UiState.Error) {
                    Text(
                        text = (uiState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // LOADING
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))


            Text(
                text = "QUICK ACTIONS",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                ActionCard(
                    title = "Check\nSymptoms",
                    icon = Icons.Default.Search
                ) {}

                ActionCard(
                    title = "Ask AI",
                    icon = Icons.Default.Chat
                ) {
                    navController.navigate(Screen.Chat.route)
                }

                ActionCard(
                    title = "History",
                    icon = Icons.Default.History
                ) {
                    navController.navigate(Screen.History.route)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            Text(
                text = "Note: This app provides general health information and is not medical advice.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}