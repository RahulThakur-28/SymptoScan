package com.rahul.symptoscan.presentation.ai.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.domain.model.HealthConversation
import com.rahul.symptoscan.presentation.ai.component.HealthConversationCard
import com.rahul.symptoscan.presentation.ai.viewmodel.HealthAssistantViewModel
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAssistantHistoryScreen(
    onBackClick: () -> Unit,
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: HealthAssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    HealthAssistantHistoryContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onConversationClick = onConversationClick,
        onNewChatClick = onNewChatClick,
        onDeleteConversation = viewModel::deleteConversation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAssistantHistoryContent(
    uiState: com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState,
    onBackClick: () -> Unit,
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onDeleteConversation: (String) -> Unit
) {
    var conversationToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Conversations", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNewChatClick) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "New Chat",
                            tint = BluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoadingConversations) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BluePrimary
                )
            } else if (uiState.conversations.isEmpty()) {
                EmptyHistoryState(onNewChatClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.conversations, key = { it.id }) { conversation ->
                        HealthConversationCard(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation.id) },
                            onDeleteClick = { conversationToDelete = conversation.id }
                        )
                    }
                }
            }

            if (conversationToDelete != null) {
                AlertDialog(
                    onDismissRequest = { conversationToDelete = null },
                    title = { Text("Delete conversation?") },
                    text = { Text("This conversation and its messages will be permanently deleted.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                conversationToDelete?.let { onDeleteConversation(it) }
                                conversationToDelete = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { conversationToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(onNewChatClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(BluePrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = BluePrimary.copy(alpha = 0.4f),
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No conversations yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start a conversation with Health Assistant to see your chats here.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNewChatClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("Start New Chat", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthAssistantHistoryScreenPreview() {
    HealthAssistantHistoryContent(
        uiState = com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState(
            conversations = listOf(
                HealthConversation("1", "u1", "Headache causes", "en", "2026-08-12T10:00:00Z", "2026-08-12T10:05:00Z"),
                HealthConversation("2", "u1", "Sleep improvement", "en", "2026-08-11T10:00:00Z", "2026-08-11T10:05:00Z")
            )
        ),
        onBackClick = {},
        onConversationClick = {},
        onNewChatClick = {},
        onDeleteConversation = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HealthAssistantHistoryScreenEmptyPreview() {
    HealthAssistantHistoryContent(
        uiState = com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState(
            conversations = emptyList()
        ),
        onBackClick = {},
        onConversationClick = {},
        onNewChatClick = {},
        onDeleteConversation = {}
    )
}
