package com.rahul.symptoscan.presentation.assessment.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.core.utils.PdfGenerator
import com.rahul.symptoscan.domain.model.FullAssessmentReport
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentReportViewModel
import com.rahul.symptoscan.ui.components.MedicalDisclaimer
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.TextDark
import com.rahul.symptoscan.ui.theme.SuccessGreen
import com.rahul.symptoscan.ui.theme.DangerRed
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentFullReportScreen(
    assessmentId: String,
    onNavigateBack: () -> Unit,
    viewModel: AssessmentReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(assessmentId) {
        viewModel.loadReport(assessmentId)
    }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Full Assessment Report", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.report != null) {
                        IconButton(onClick = { sharePdf(context, uiState.report!!) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Unable to load this report.", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadReport(assessmentId) }) {
                        Text("Retry")
                    }
                }
            }
        } else if (uiState.report != null) {
            val report = uiState.report!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        ReportSummaryCard(report)
                    }

                    item {
                        SectionHeader("Symptoms")
                    }
                    items(report.symptoms) { symptom ->
                        SymptomCard(
                            name = symptom.symptomName,
                            severity = symptom.severity ?: 0,
                            duration = symptom.duration ?: "N/A",
                            frequency = symptom.frequency ?: "N/A",
                            isCustom = symptom.isCustom
                        )
                    }

                    if (report.assessment.bodyTemperature != null || !report.assessment.additionalNotes.isNullOrBlank()) {
                        item {
                            SectionHeader("Additional Context")
                        }
                        item {
                            ContextCard(
                                temperature = report.assessment.bodyTemperature,
                                notes = report.assessment.additionalNotes
                            )
                        }
                    }

                    item {
                        SectionHeader("AI Analysis")
                    }
                    item {
                        AnalysisCard(report)
                    }

                    if (report.questions.isNotEmpty()) {
                        item {
                            SectionHeader("Follow-up Questions")
                        }
                        items(report.questions) { qa ->
                            QuestionAnswerItem(qa.question, qa.answer ?: "No answer provided")
                        }
                    }

                    item {
                        MedicalDisclaimer()
                    }
                }

                Surface(
                    color = Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    PrimaryButton(
                        text = "Download PDF Report",
                        onClick = { 
                            val file = PdfGenerator.generateAssessmentPdf(context, report)
                            scope.launch {
                                if (file != null) {
                                    snackbarHostState.showSnackbar("Assessment PDF saved successfully.")
                                } else {
                                    snackbarHostState.showSnackbar("Unable to generate PDF.")
                                }
                            }
                        },
                        modifier = Modifier.padding(24.dp),
                        icon = Icons.Default.Download
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ReportSummaryCard(report: FullAssessmentReport) {
    val urgencyColor = when(report.result.urgencyLevel?.lowercase()) {
        "emergency", "urgent" -> DangerRed
        "routine" -> BluePrimary
        else -> SuccessGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = report.result.urgencyLevel?.uppercase() ?: "UNKNOWN",
                color = urgencyColor,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Health Assessment Report",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Completed on ${report.assessment.createdAt ?: "N/A"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun SymptomCard(name: String, severity: Int, duration: String, frequency: String, isCustom: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (isCustom) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(4.dp)) {
                        Text("CUSTOM", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Severity: $severity/10 • $duration • $frequency", fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun ContextCard(temperature: Double?, notes: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (temperature != null) {
                Text("Body Temperature", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("$temperature°C", fontSize = 14.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (!notes.isNullOrBlank()) {
                Text("Additional Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(notes, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun AnalysisCard(report: FullAssessmentReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("AI Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(report.result.summary ?: "", fontSize = 14.sp, lineHeight = 20.sp, color = Color.DarkGray)
            
            if (!report.result.possibleCauses.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Possible Explanations", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                report.result.possibleCauses!!.forEach { cause ->
                    Text("• $cause", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(vertical = 2.dp))
                }
            }

            if (!report.result.recommendations.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recommendations", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                report.result.recommendations!!.forEach { rec ->
                    Text("✓ $rec", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun QuestionAnswerItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("Q: $question", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
        Text("A: $answer", fontSize = 14.sp, color = Color.DarkGray)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.2f))
    }
}

private fun sharePdf(context: Context, report: FullAssessmentReport) {
    val file = PdfGenerator.generateAssessmentPdf(context, report)
    if (file != null) {
        val uri = FileProvider.getUriForFile(context, "com.rahul.symptoscan.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Assessment Report"))
    }
}
