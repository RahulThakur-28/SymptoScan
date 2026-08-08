package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.rahul.symptoscan.domain.model.AssessmentReport
import com.rahul.symptoscan.domain.model.PatientInfo
import com.rahul.symptoscan.domain.model.ReportedSymptom
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentReportViewModel
import com.rahul.symptoscan.ui.components.MedicalDisclaimer
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentReportScreen(
    assessmentId: String,
    onNavigateBack: () -> Unit,
    viewModel: AssessmentReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(assessmentId) {
        viewModel.loadReport(assessmentId)
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Assessment Report", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
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
                    Text(text = uiState.error!!, color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadReport(assessmentId) }) {
                        Text("Retry")
                    }
                }
            }
        } else if (uiState.report != null) {
            ReportContent(
                report = uiState.report!!,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ReportContent(
    report: AssessmentReport,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ReportHeroCard(report = report)
        }

        item {
            PatientInformationCard(patientInfo = report.patientInfo)
        }

        item {
            AssessmentSummaryCard(report = report)
        }

        item {
            ReportedSymptomsCard(symptoms = report.reportedSymptoms)
        }

        item {
            ClinicalInsightsCard(insights = report.clinicalInsights)
        }

        item {
            MedicalDisclaimer()
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = "Download PDF",
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(Dimens.CornerRadiusMedium)
                ) {
                    Text("Share")
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportHeroCard(report: AssessmentReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(DeepBlue, BluePrimary, CyanBlue)
                    )
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📋", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        "SYMPTOSCAN REPORT",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${report.assessmentTitle} Assessment",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${report.riskLevel} • Score: ${report.riskScore}",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientInformationCard(patientInfo: PatientInfo) {
    SectionCard(title = "PATIENT INFORMATION") {
        InfoRow(label = "Name", value = patientInfo.name)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
        InfoRow(label = "DOB", value = "${patientInfo.dob} (${patientInfo.age} yrs)")
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
        InfoRow(label = "Blood Group", value = patientInfo.bloodGroup ?: "Not provided")
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
        InfoRow(label = "Gender", value = patientInfo.gender)
    }
}

@Composable
private fun AssessmentSummaryCard(report: AssessmentReport) {
    SectionCard(title = "ASSESSMENT SUMMARY") {
        InfoRow(label = "Date", value = report.assessmentDate)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
        InfoRow(label = "Duration", value = report.duration)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
        InfoRow(label = "Symptoms Assessed", value = report.symptomsAssessedCount.toString())
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
        InfoRow(label = "Risk Score", value = "${report.riskScore}/100 (${report.riskLevel})")
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
        InfoRow(label = "Confidence", value = "${report.confidence}%")
    }
}

@Composable
private fun ReportedSymptomsCard(symptoms: List<ReportedSymptom>) {
    SectionCard(title = "REPORTED SYMPTOMS") {
        symptoms.forEachIndexed { index, symptom ->
            SymptomReportRow(symptom = symptom)
            if (index < symptoms.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
private fun SymptomReportRow(symptom: ReportedSymptom) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Text(symptom.icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = symptom.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(
                text = "${symptom.duration} • Pain: ${symptom.painLevel}/10",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
        Surface(
            color = if (symptom.severity == "Mild") Color(0xFFF0FDF4) else Color(0xFFFFF7ED),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = symptom.severity,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (symptom.severity == "Mild") SuccessGreen else WarningAmber
            )
        }
    }
}

@Composable
private fun ClinicalInsightsCard(insights: String) {
    SectionCard(title = "AI CLINICAL INSIGHTS") {
        Text(
            text = insights,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = TextDark
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Preview(showBackground = true)
@Composable
fun AssessmentReportScreenPreview() {
    SymptoScanTheme {
        val sampleReport = AssessmentReport(
            id = "123",
            assessmentTitle = "Headache & Fatigue",
            riskScore = 34,
            riskLevel = "Low Risk",
            confidence = 78,
            patientInfo = PatientInfo(
                name = "Sarah Johnson",
                dob = "March 15, 1992",
                age = 32,
                bloodGroup = "O+",
                gender = "Female"
            ),
            assessmentDate = "Sat, Aug 8, 2026",
            duration = "~12 minutes",
            symptomsAssessedCount = 3,
            reportedSymptoms = listOf(
                ReportedSymptom("s1", "Headache", "🤕", "2–3 days", 5, "Moderate"),
                ReportedSymptom("s2", "Fatigue", "😴", "2–3 days", 3, "Mild"),
                ReportedSymptom("s3", "Fever", "🤒", "Today", 2, "Mild")
            ),
            clinicalInsights = "The presented symptom cluster — headache with associated fatigue and mild low-grade fever — demonstrates a pattern consistent with tension-type headache with possible early viral illness. The gradual onset (2–3 days), intermittent pattern, and mild severity (5/10) suggest a self-limiting condition without immediate red flags.\\n\\nKey reassuring factors include the absence of sudden severe onset (\\\"thunderclap\\\"), neurological symptoms, or constitutional symptoms such as significant weight loss or night sweats."
        )
        ReportContent(report = sampleReport)
    }
}
