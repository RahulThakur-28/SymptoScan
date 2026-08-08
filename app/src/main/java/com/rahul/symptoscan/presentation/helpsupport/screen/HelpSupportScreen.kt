package com.rahul.symptoscan.presentation.helpsupport.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.presentation.helpsupport.component.FaqItem
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.DangerRed
import com.rahul.symptoscan.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmergency: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Help & Support", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            SectionTitle("FAQ")
            
            val faqs = listOf(
                "What is SymptomScan?" to "SymptomScan is an AI-powered health assessment tool that helps you understand your symptoms and provides guidance on the next steps.",
                "How does the assessment work?" to "The app asks a series of questions about your symptoms and history, then uses medical AI to provide a summary and risk level.",
                "How is my health information protected?" to "Your data is encrypted and stored securely. We prioritize your privacy and do not share your sensitive medical data with third parties.",
                "Can I delete my account?" to "Yes, you can permanently delete your account and all associated data from the Data Management section in Settings.",
                "Is the AI Assistant a doctor?" to "No, the AI Assistant is an informational tool only. It does not provide medical diagnosis or professional advice."
            )
            
            faqs.forEach { (q, a) ->
                FaqItem(question = q, answer = a)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("CONTACT SUPPORT")
            
            SupportActionCard(
                title = "Email Support",
                subtitle = "Response within 24 hours",
                icon = Icons.Outlined.Email,
                onClick = { }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SupportActionCard(
                title = "Report a Problem",
                subtitle = "Help us improve SymptomScan",
                icon = Icons.Outlined.ReportProblem,
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("EMERGENCY HELP")
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Emergency, contentDescription = null, tint = DangerRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Need immediate medical assistance?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToEmergency,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Open Emergency Mode")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SupportActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}
