package com.rahul.symptoscan.presentation.assessment.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.filled.Search
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
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomAssessmentScreen(
    onNavigate: (String) -> Unit,
    onNavigateToDetails: () -> Unit,
    viewModel: AssessmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            HomeBottomNavigation(
                currentRoute = "assess",
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Symptom Assessment",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Select all symptoms you're experiencing",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search symptoms...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { IconButton(onClick = {}) { Icon(Icons.Rounded.Mic, contentDescription = "Voice search") } },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            val showAddCustom = uiState.searchQuery.isNotBlank() && 
                !uiState.symptoms.any { it.name.equals(uiState.searchQuery, ignoreCase = true) } &&
                !uiState.selectedSymptoms.any { it.name.equals(uiState.searchQuery, ignoreCase = true) }

            if (showAddCustom) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.addCustomSymptom(uiState.searchQuery) },
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
                    color = Color(0xFF1E293B)
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
                            onClick = { viewModel.onSymptomToggle(symptom.id) },
                            label = { Text(symptom.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary.copy(alpha = 0.1f),
                                selectedLabelColor = BluePrimary
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
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                val filteredSymptoms = uiState.symptoms.filter { 
                    it.name.contains(uiState.searchQuery, ignoreCase = true) 
                }
                
                items(filteredSymptoms) { symptom ->
                    val isSelected = uiState.selectedSymptoms.any { it.id == symptom.id }
                    SymptomCard(
                        symptom = symptom.copy(isSelected = isSelected),
                        onClick = { viewModel.onSymptomToggle(symptom.id) }
                    )
                }
                
                // Show custom symptoms at the end if they are selected
                items(uiState.selectedSymptoms.filter { it.category == "Custom" }) { symptom ->
                    SymptomCard(
                        symptom = symptom.copy(isSelected = true),
                        onClick = { viewModel.onSymptomToggle(symptom.id) }
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
}
