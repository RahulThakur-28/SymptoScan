package com.rahul.symptoscan.presentation.history.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigate: (String) -> Unit,
    onAssessmentClick: (String) -> Unit,
    viewModel: HistoryViewModel = viewModel(),
    showScaffold: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onRefresh()
    }

    if (showScaffold) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                HomeBottomNavigation(
                    currentRoute = "history",
                    onNavigate = onNavigate
                )
            },
            topBar = {
                Surface(shadowElevation = 2.dp) {
                    TopAppBar(
                        title = { 
                            Column {
                                Text(
                                    text = "History", 
                                    fontSize = 20.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Your previous health assessments",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        ) { paddingValues ->
            HistoryScreenContent(
                paddingValues = paddingValues,
                uiState = uiState,
                onRefresh = viewModel::onRefresh,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onFilterSelected = viewModel::onFilterSelected,
                onAssessmentClick = onAssessmentClick,
                onNavigate = onNavigate
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(shadowElevation = 2.dp) {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = "History", 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Your previous health assessments",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
            HistoryScreenContent(
                paddingValues = PaddingValues(0.dp),
                uiState = uiState,
                onRefresh = viewModel::onRefresh,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onFilterSelected = viewModel::onFilterSelected,
                onAssessmentClick = onAssessmentClick,
                onNavigate = onNavigate
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenContent(
    paddingValues: PaddingValues,
    uiState: com.rahul.symptoscan.presentation.history.state.HistoryUiState,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (AssessmentStatus?) -> Unit,
    onAssessmentClick: (String) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = uiState.isRefreshing,
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                placeholder = { 
                    Text(
                        "Search assessments...", 
                        fontSize = 14.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ) 
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterItem(
                    label = "All",
                    selected = uiState.selectedFilter == null,
                    onClick = { onFilterSelected(null) }
                )
                FilterItem(
                    label = "Low",
                    selected = uiState.selectedFilter == AssessmentStatus.Low,
                    onClick = { onFilterSelected(AssessmentStatus.Low) },
                    accentColor = SuccessGreen
                )
                FilterItem(
                    label = "Moderate",
                    selected = uiState.selectedFilter == AssessmentStatus.Moderate,
                    onClick = { onFilterSelected(AssessmentStatus.Moderate) },
                    accentColor = WarningAmber
                )
                FilterItem(
                    label = "High",
                    selected = uiState.selectedFilter == AssessmentStatus.High,
                    onClick = { onFilterSelected(AssessmentStatus.High) },
                    accentColor = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        HistoryStats(
                            total = uiState.totalCount,
                            low = uiState.lowCount,
                            moderate = uiState.moderateCount,
                            high = uiState.highCount
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (uiState.filteredAssessments.isEmpty()) {
                        item {
                            EmptyHistoryState(
                                isSearch = uiState.searchQuery.isNotEmpty() || uiState.selectedFilter != null,
                                onStartAssessment = { onNavigate("assess") }
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "${uiState.filteredAssessments.size} ASSESSMENTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                letterSpacing = 1.sp
                            )
                        }

                        itemsIndexed(
                            items = uiState.filteredAssessments,
                            key = { _, item -> item.id }
                        ) { _, assessment ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                AssessmentHistoryCard(
                                    assessment = assessment,
                                    onClick = { onAssessmentClick(assessment.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp)),
        color = if (selected) accentColor else MaterialTheme.colorScheme.surface,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyHistoryState(
    isSearch: Boolean,
    onStartAssessment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearch) Icons.Default.Search else Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (isSearch) "No matching assessments" else "No assessments yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isSearch) "Try changing your search query or filter to find what you're looking for." 
                  else "Your completed AI health assessments will appear here for easy tracking.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        if (!isSearch) {
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = "Start New Assessment",
                onClick = onStartAssessment,
                modifier = Modifier.width(220.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    SymptoScanTheme {
        HistoryScreen(
            onNavigate = {},
            onAssessmentClick = {}
        )
    }
}
