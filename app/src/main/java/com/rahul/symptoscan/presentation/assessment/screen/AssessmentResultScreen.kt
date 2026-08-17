package com.rahul.symptoscan.presentation.assessment.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.core.utils.PdfGenerator
import com.rahul.symptoscan.data.remote.model.DbAssessmentResult
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssessmentResultScreen(
    onNavigateBack: () -> Unit,
    onViewFullReport: () -> Unit,
    onAskAI: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val result = uiState.result
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    SideEffect {
        val window = (view.context as android.app.Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                shadowElevation = 0.dp, 
                color = MaterialTheme.colorScheme.background
            ) {
                TopAppBar(
                    title = { Text("Assessment Complete", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                    IconButton(onClick = {
                        if (result != null) {
                            val shareText = "My SymptoScan Assessment Result: ${deriveRiskLevelText(result.urgencyLevel)} (${result.riskScore ?: deriveRiskScore(result.urgencyLevel)}/100). Summary: ${result.summary}"
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = TopAppBarDefaults.windowInsets
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BluePrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Finalizing your report...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (result != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                ResultHero(result, uiState.selectedSymptoms.size)
                
                Column(modifier = Modifier.padding(24.dp)) {
                    AiExplanationSection(result.summary ?: "")
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    PossibleConditionsSection(result)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    RecommendationsSection(result)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    SpecialistSection(result)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    DisclaimerSection(result.disclaimer ?: "")
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    PrimaryButton(
                        text = "View Full Report",
                        onClick = onViewFullReport
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { previewPdf(context, result, uiState.selectedSymptoms.map { it.name }) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Preview PDF")
                        }
                        
                        OutlinedButton(
                            onClick = { downloadPdf(context, result, uiState.selectedSymptoms.map { it.name }) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    TextButton(
                        onClick = onAskAI,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Discuss with AI Assistant", fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultHero(result: DbAssessmentResult, symptomCount: Int) {
    val riskScore = result.riskScore
    val riskLevel = deriveRiskLevelText(result.urgencyLevel)
    val riskColor = deriveRiskColor(result.urgencyLevel)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { (riskScore ?: 0) / 100f },
                modifier = Modifier.size(160.dp),
                color = riskColor,
                strokeWidth = 12.dp,
                trackColor = riskColor.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = riskScore?.toString() ?: "--",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = riskLevel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            color = riskColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(riskColor))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$riskLevel Assessment",
                    color = riskColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        Text(
            text = "Based on $symptomCount symptoms • $dateStr",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AiExplanationSection(explanation: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "AI Explanation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Text(
                text = explanation,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun PossibleConditionsSection(result: DbAssessmentResult) {
    Column {
        Text(
            text = "POSSIBLE CONDITIONS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        val conditions = result.conditions ?: deriveConditions(result.possibleCauses)
        
        conditions.forEach { condition ->
            ConditionCard(condition)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ConditionCard(condition: com.rahul.symptoscan.data.remote.model.DbAssessmentCondition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        color = BluePrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = condition.icon ?: "🤒", fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = condition.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = condition.severity,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Text(
                    text = "${condition.confidence}%",
                    fontWeight = FontWeight.Black,
                    color = BluePrimary,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column {
                LinearProgressIndicator(
                    progress = { condition.confidence / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = BluePrimary,
                    trackColor = BluePrimary.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Confidence",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RecommendationsSection(result: DbAssessmentResult) {
    Text(
        text = "RECOMMENDATIONS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    val recs = result.recommendationItems ?: result.recommendations?.map { com.rahul.symptoscan.data.remote.model.DbRecommendationItem(it, "✅") } ?: emptyList()
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        recs.forEach { rec ->
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = rec.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = rec.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun SpecialistSection(result: DbAssessmentResult) {
    val specialist = result.specialist ?: com.rahul.symptoscan.data.remote.model.DbRecommendedSpecialist(
        "General Practitioner",
        "We recommend consulting a general practitioner for further evaluation of your symptoms."
    )
    
    Text(
        text = "RECOMMENDED SPECIALIST",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = specialist.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = specialist.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun DisclaimerSection(disclaimer: String) {
    Surface(
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = disclaimer,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun deriveRiskScore(urgency: String?): Int = when (urgency?.lowercase()) {
    "emergency" -> 92
    "urgent" -> 74
    "moderate" -> 48
    "routine" -> 32
    else -> 18
}

private fun deriveRiskLevelText(urgency: String?): String = when (urgency?.lowercase()) {
    "emergency" -> "High Risk"
    "urgent" -> "High Risk"
    "moderate" -> "Moderate Risk"
    "routine" -> "Low Risk"
    else -> "Low Risk"
}

private fun deriveRiskColor(urgency: String?): Color = when (urgency?.lowercase()) {
    "emergency", "urgent" -> DangerRed
    "moderate" -> WarningAmber
    else -> SuccessGreen
}

private fun deriveConditions(causes: List<String>?): List<com.rahul.symptoscan.data.remote.model.DbAssessmentCondition> {
    return causes?.mapIndexed { index, cause ->
        val confidence = if (index == 0) 85 else if (index == 1) 65 else 45
        com.rahul.symptoscan.data.remote.model.DbAssessmentCondition(cause, "Mild", confidence, "🤒")
    } ?: emptyList()
}

private fun previewPdf(context: Context, result: DbAssessmentResult, symptoms: List<String>) {
    val file = PdfGenerator.generateAssessmentPdf(context, result, symptoms)
    if (file != null) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun downloadPdf(context: Context, result: DbAssessmentResult, symptoms: List<String>) {
    val file = PdfGenerator.generateAssessmentPdf(context, result, symptoms)
    if (file != null) {
        val success = PdfGenerator.savePdfToDownloads(context, file)
        if (success) {
            Toast.makeText(context, "Report saved to Downloads", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Failed to save report", Toast.LENGTH_SHORT).show()
        }
    }
}
