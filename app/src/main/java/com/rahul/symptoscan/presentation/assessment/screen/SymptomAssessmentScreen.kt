package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.assessment.component.SymptomCard
import com.rahul.symptoscan.presentation.assessment.viewmodel.AssessmentViewModel
import com.rahul.symptoscan.presentation.home.component.HomeBottomNavigation
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomAssessmentScreen(
    onNavigate: (String) -> Unit,
    onNavigateToDetails: () -> Unit,
    viewModel: AssessmentViewModel = viewModel(),
    showScaffold: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (showScaffold) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                HomeBottomNavigation(
                    currentRoute = "assess",
                    onNavigate = onNavigate
                )
            },
            topBar = {
                Surface(shadowElevation = 2.dp) {
                    TopAppBar(
                        title = { 
                            Text(
                                "New Assessment", 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        ) { paddingValues ->
            SymptomAssessmentContent(
                paddingValues = paddingValues,
                uiState = uiState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onAddCustomSymptom = viewModel::addCustomSymptom,
                onSymptomToggle = viewModel::onSymptomToggle,
                onNavigateToDetails = onNavigateToDetails
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(shadowElevation = 2.dp) {
                TopAppBar(
                    title = { 
                        Text(
                            "New Assessment", 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
            SymptomAssessmentContent(
                paddingValues = PaddingValues(0.dp),
                uiState = uiState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onAddCustomSymptom = viewModel::addCustomSymptom,
                onSymptomToggle = viewModel::onSymptomToggle,
                onNavigateToDetails = onNavigateToDetails
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomAssessmentContent(
    paddingValues: PaddingValues,
    uiState: com.rahul.symptoscan.presentation.assessment.state.AssessmentUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddCustomSymptom: (String) -> Unit,
    onSymptomToggle: (String) -> Unit,
    onNavigateToDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "What symptoms are you feeling?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Select all that apply for a comprehensive AI analysis.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search symptoms...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )

        val showAddCustom = uiState.searchQuery.isNotBlank() && 
            !uiState.symptoms.any { it.name.equals(uiState.searchQuery, ignoreCase = true) } &&
            !uiState.selectedSymptoms.any { it.name.equals(uiState.searchQuery, ignoreCase = true) }

        if (showAddCustom) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onAddCustomSymptom(uiState.searchQuery) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary.copy(alpha = 0.1f), 
                    contentColor = BluePrimary
                )
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add \"${uiState.searchQuery}\" as custom symptom")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.recentSymptoms.isNotEmpty()) {
            Text(
                text = "Recent Symptoms",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.recentSymptoms.forEach { symptom ->
                    val isSelected = uiState.selectedSymptoms.any { it.id == symptom.id }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSymptomToggle(symptom.id) },
                        label = { 
                            Text(
                                text = symptom.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 1.dp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = "Symptoms List",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            val filteredSymptoms = uiState.symptoms.filter { 
                it.name.contains(uiState.searchQuery, ignoreCase = true) 
            }
            
            items(filteredSymptoms) { symptom ->
                val isSelected = uiState.selectedSymptoms.any { it.id == symptom.id }
                SymptomCard(
                    symptom = symptom.copy(isSelected = isSelected),
                    onClick = { onSymptomToggle(symptom.id) }
                )
            }
            
            // Show custom symptoms at the end if they are selected
            items(uiState.selectedSymptoms.filter { it.category == "Custom" }) { symptom ->
                SymptomCard(
                    symptom = symptom.copy(isSelected = true),
                    onClick = { onSymptomToggle(symptom.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "Continue" + if(uiState.selectedSymptoms.isNotEmpty()) " (${uiState.selectedSymptoms.size})" else "",
            onClick = onNavigateToDetails,
            enabled = uiState.selectedSymptoms.isNotEmpty(),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
