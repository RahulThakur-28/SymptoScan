package com.rahul.symptoscan.presentation.history.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.rahul.symptoscan.domain.model.AssessmentStatus
import com.rahul.symptoscan.presentation.home.component.HomeBottomNavigation
import com.rahul.symptoscan.presentation.history.component.AssessmentHistoryCard
import com.rahul.symptoscan.presentation.history.component.HistoryStats
import com.rahul.symptoscan.presentation.history.viewmodel.HistoryViewModel
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigate: (String) -> Unit,
    onAssessmentClick: (String) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            HomeBottomNavigation(
                currentRoute = "history",
                onNavigate = onNavigate
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("History", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF))
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = BluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                placeholder = { Text("Search assessments...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f)
                ),
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterItem(
                    label = "All",
                    selected = uiState.selectedFilter == null,
                    onClick = { viewModel.onFilterSelected(null) }
                )
                FilterItem(
                    label = "Low",
                    selected = uiState.selectedFilter == AssessmentStatus.Low,
                    onClick = { viewModel.onFilterSelected(AssessmentStatus.Low) }
                )
                FilterItem(
                    label = "Moderate",
                    selected = uiState.selectedFilter == AssessmentStatus.Moderate,
                    onClick = { viewModel.onFilterSelected(AssessmentStatus.Moderate) }
                )
                FilterItem(
                    label = "High",
                    selected = uiState.selectedFilter == AssessmentStatus.High,
                    onClick = { viewModel.onFilterSelected(AssessmentStatus.High) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            HistoryStats(
                total = uiState.totalCount,
                low = uiState.lowCount,
                moderate = uiState.moderateCount,
                high = uiState.highCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${uiState.filteredAssessments.size} ASSESSMENTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 24.dp),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else if (uiState.filteredAssessments.isEmpty()) {
                EmptyHistoryState(
                    isSearch = uiState.searchQuery.isNotEmpty() || uiState.selectedFilter != null,
                    onStartAssessment = { onNavigate("assess") }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(
                        items = uiState.filteredAssessments,
                        key = { _, item -> item.id }
                    ) { index, assessment ->
                        TimelineAssessmentItem(
                            assessment = assessment,
                            isLast = index == uiState.filteredAssessments.size - 1,
                            onClick = { onAssessmentClick(assessment.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TimelineAssessmentItem(
    assessment: com.rahul.symptoscan.domain.model.AssessmentSummary,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (assessment.status) {
        AssessmentStatus.Low -> Color(0xFF16C76B)
        AssessmentStatus.Moderate -> Color(0xFFF59E0B)
        AssessmentStatus.High -> Color(0xFFEF4444)
    }

    val icon = when (assessment.status) {
        AssessmentStatus.Low -> Icons.Rounded.Check
        else -> Icons.Rounded.Warning
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f))
                    .border(1.dp, statusColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }
        }
        
        // Card Column
        Column(modifier = Modifier.weight(1f)) {
            AssessmentHistoryCard(
                assessment = assessment,
                onClick = onClick
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FilterItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) BluePrimary else Color(0xFFEFF6FF))
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else BluePrimary
        )
    }
}

@Composable
private fun EmptyHistoryState(
    isSearch: Boolean,
    onStartAssessment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSearch) "No matching assessments" else "No assessments yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearch) "Try changing your search or filter." else "Your completed symptom assessments will appear here.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (!isSearch) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartAssessment,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Start Assessment")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    MaterialTheme {
        HistoryScreen(
            onNavigate = {},
            onAssessmentClick = {}
        )
    }
}
