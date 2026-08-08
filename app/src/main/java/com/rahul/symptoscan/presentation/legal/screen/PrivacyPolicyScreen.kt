package com.rahul.symptoscan.presentation.legal.screen

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
import com.rahul.symptoscan.presentation.legal.component.LegalBulletList
import com.rahul.symptoscan.presentation.legal.component.LegalParagraph
import com.rahul.symptoscan.presentation.legal.component.LegalSection
import com.rahul.symptoscan.ui.theme.BackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            LegalSection(title = "1. Information We Collect") {
                LegalParagraph("We collect information to provide better services to all our users. The types of information we collect include:")
                LegalBulletList(listOf(
                    "Account Information: Name, email address, and authentication credentials.",
                    "Health Profile: Age, gender, height, weight, and blood group.",
                    "Medical Data: Reported symptoms, assessment history, and AI-generated insights."
                ))
            }
            
            LegalSection(title = "2. How We Use Your Information") {
                LegalParagraph("We use the information we collect for the following purposes:")
                LegalBulletList(listOf(
                    "To provide and maintain our health assessment services.",
                    "To personalize your experience and provide relevant health guidance.",
                    "To improve our AI algorithms and app functionality.",
                    "To communicate with you about your account and updates."
                ))
            }
            
            LegalSection(title = "3. AI Processing") {
                LegalParagraph("Your symptom reports are processed by our Medical AI to provide assessments. This processing is informational and does not constitute a medical diagnosis.")
            }
            
            LegalSection(title = "4. Data Security") {
                LegalParagraph("We implement a variety of security measures to maintain the safety of your personal information. Your sensitive health data is encrypted and stored securely.")
            }
            
            LegalSection(title = "5. Contact") {
                LegalParagraph("If you have any questions about this Privacy Policy, please contact us at support@symptoscan.com")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
