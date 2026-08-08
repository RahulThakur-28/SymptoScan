package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.ui.components.MedicalDisclaimer
import com.rahul.symptoscan.ui.theme.BackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val result = uiState.result ?: return

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Health Report", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
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
            Text("Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ReportRow(label = "Date", value = result.date)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    ReportRow(label = "Risk Level", value = result.riskLevel)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    ReportRow(label = "Risk Score", value = "${result.riskScore}/100")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Selected Symptoms", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    uiState.selectedSymptoms.forEachIndexed { index, symptom ->
                        Text("${index + 1}. ${symptom.name}", fontSize = 14.sp)
                        if (index < (uiState.selectedSymptoms.size - 1)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text("Detailed Analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = result.aiExplanation,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            MedicalDisclaimer()
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
