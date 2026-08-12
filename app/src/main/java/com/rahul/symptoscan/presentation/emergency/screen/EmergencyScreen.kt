package com.rahul.symptoscan.presentation.emergency.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.domain.model.EmergencyContact
import com.rahul.symptoscan.presentation.emergency.viewmodel.EmergencyViewModel
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.DangerRed
import com.rahul.symptoscan.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onNavigateBack: () -> Unit,
    onAddEmergencyContact: () -> Unit,
    onEditEmergencyContact: () -> Unit,
    viewModel: EmergencyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadEmergencyContact()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Emergency Mode", color = DangerRed, fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Emergency, contentDescription = null, tint = DangerRed, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Emergency Assistance",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            
            Text(
                text = "Please stay calm. Help is available.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CALL EMERGENCY SERVICES
            EmergencyActionCard(
                title = "Call Emergency Services",
                subtitle = "National Emergency Number",
                number = "911",
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // EMERGENCY CONTACT
            Text(
                text = "EMERGENCY CONTACT",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.emergencyContact != null) {
                val contact = uiState.emergencyContact!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(contact.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = DangerRed)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(contact.relationship, fontSize = 13.sp, color = Color.Gray)
                            }
                            IconButton(onClick = onEditEmergencyContact) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Call Contact")
                            }
                            
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Text("Remove", color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                Card(
                    onClick = onAddEmergencyContact,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No emergency contact added", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "+ Add Emergency Contact",
                            color = DangerRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // FIRST AID GUIDANCE
            SectionHeader(title = "First Aid Guidance")
            FirstAidItem("Shortness of Breath", "Keep the person seated, upright, and calm. Loosen tight clothing.")
            FirstAidItem("Chest Pain", "Sit the person down, keep them calm. If they have prescribed nitroglycerin, assist them.")
            FirstAidItem("Severe Bleeding", "Apply direct pressure with a clean cloth. Elevate the injured area.")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // EMERGENCY WARNING SIGNS
            SectionHeader(title = "Emergency Warning Signs")
            WarningSign("Difficulty breathing or severe shortness of breath")
            WarningSign("Persistent pain or pressure in the chest")
            WarningSign("New confusion or inability to wake/stay awake")
            WarningSign("Pale, gray, or blue-colored skin, lips, or nail beds")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove emergency contact?") },
            text = { Text("Are you sure you want to remove this emergency contact?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeEmergencyContact()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 1.sp
    )
}

@Composable
private fun EmergencyActionCard(
    title: String,
    subtitle: String,
    number: String,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DangerRed)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Text(text = number, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(text = subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            FloatingActionButton(
                onClick = onCall,
                containerColor = Color.White,
                contentColor = DangerRed,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
        }
    }
}

@Composable
private fun FirstAidItem(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun WarningSign(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Icon(Icons.Rounded.Info, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 13.sp, color = Color.Gray)
    }
}
