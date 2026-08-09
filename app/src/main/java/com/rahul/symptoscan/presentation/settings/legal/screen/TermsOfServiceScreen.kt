package com.rahul.symptoscan.presentation.settings.legal.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.presentation.settings.legal.component.LegalParagraph
import com.rahul.symptoscan.presentation.settings.legal.component.LegalSection
import com.rahul.symptoscan.ui.theme.BackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            LegalSection(title = "1. Acceptance of Terms") {
                LegalParagraph("By accessing or using SymptomScan, you agree to be bound by these Terms of Service. If you do not agree, please do not use the application.")
            }
            
            LegalSection(title = "2. Medical Disclaimer") {
                LegalParagraph("SymptomScan provides general health information and AI-powered assessments for informational purposes only. It is NOT a substitute for professional medical advice, diagnosis, or treatment.")
                LegalParagraph("In case of a medical emergency, call your local emergency services immediately.")
            }
            
            LegalSection(title = "3. User Responsibilities") {
                LegalParagraph("You are responsible for the accuracy of the information you provide and for maintaining the confidentiality of your account credentials.")
            }
            
            LegalSection(title = "4. Limitation of Liability") {
                LegalParagraph("To the maximum extent permitted by law, SymptomScan shall not be liable for any direct, indirect, incidental, or consequential damages resulting from your use of the service.")
            }
            
            LegalSection(title = "5. Changes to Terms") {
                LegalParagraph("We reserve the right to modify these terms at any time. Your continued use of the app constitutes acceptance of the updated terms.")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
