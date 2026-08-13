package com.rahul.symptoscan.presentation.ai.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
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
import com.rahul.symptoscan.domain.model.HealthMessage
import com.rahul.symptoscan.presentation.ai.component.HealthAssistantEmptyState
import com.rahul.symptoscan.presentation.ai.component.HealthAssistantInput
import com.rahul.symptoscan.presentation.ai.component.HealthMessageBubble
import com.rahul.symptoscan.presentation.ai.viewmodel.HealthAssistantViewModel
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.TextDark

import com.rahul.symptoscan.presentation.home.component.HomeBottomNavigation
import com.rahul.symptoscan.ui.theme.BackgroundLight
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAssistantScreen(
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: HealthAssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    HealthAssistantContent(
        uiState = uiState,
        onNavigate = onNavigate,
        onBackClick = onBackClick,
        onHistoryClick = onHistoryClick,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::sendMessage,
        onNewChatClick = viewModel::startNewConversation,
        onSuggestionClick = { suggestion ->
            viewModel.onInputChange(suggestion)
            viewModel.sendMessage()
        },
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAssistantContent(
    uiState: com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState,
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onClearError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isSendingMessage) {
        if (uiState.messages.isNotEmpty()) {
            val totalItems = uiState.messages.size + (if (uiState.isSendingMessage) 1 else 0)
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Health Assistant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "General health information",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            Icons.Default.History, 
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    TextButton(
                        onClick = onNewChatClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (uiState.messages.isEmpty()) {
                HomeBottomNavigation(
                    currentRoute = "ai",
                    onNavigate = onNavigate
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.messages.isEmpty() && !uiState.isLoadingMessages) {
                    HealthAssistantEmptyState(
                        suggestions = listOf(
                            "Common causes of headache",
                            "How can I improve my sleep?",
                            "What can cause fatigue?",
                            "When should I see a doctor for a cough?"
                        ),
                        onSuggestionClick = onSuggestionClick
                    )
                } else if (uiState.isLoadingMessages) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.messages) { message ->
                            HealthMessageBubble(message = message)
                        }

                        if (uiState.isSendingMessage) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }
                }
            }

            HealthAssistantInput(
                value = uiState.inputText,
                onValueChange = onInputChange,
                onSendClick = onSendClick,
                isEnabled = true,
                isSending = uiState.isSendingMessage
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.85f)) {
            Text(
                text = "Health Assistant",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BluePrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thinking...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthAssistantScreenEmptyPreview() {
    HealthAssistantContent(
        uiState = com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState(
            messages = emptyList(),
            isLoadingMessages = false
        ),
        onNavigate = {},
        onBackClick = {},
        onHistoryClick = {},
        onInputChange = {},
        onSendClick = {},
        onNewChatClick = {},
        onSuggestionClick = {},
        onClearError = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HealthAssistantScreenChatPreview() {
    HealthAssistantContent(
        uiState = com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState(
            messages = listOf(
                HealthMessage("1", "c1", "u1", "user", "What can cause fatigue?", null),
                HealthMessage("2", "c1", "u1", "assistant", "Fatigue can be caused by many factors, including lack of sleep, stress, poor diet, or underlying medical conditions like anemia or thyroid issues.", null)
            ),
            isLoadingMessages = false
        ),
        onNavigate = {},
        onBackClick = {},
        onHistoryClick = {},
        onInputChange = {},
        onSendClick = {},
        onNewChatClick = {},
        onSuggestionClick = {},
        onClearError = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HealthAssistantScreenLoadingPreview() {
    HealthAssistantContent(
        uiState = com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState(
            messages = listOf(
                HealthMessage("1", "c1", "u1", "user", "How to improve sleep?", null)
            ),
            isSendingMessage = true
        ),
        onNavigate = {},
        onBackClick = {},
        onHistoryClick = {},
        onInputChange = {},
        onSendClick = {},
        onNewChatClick = {},
        onSuggestionClick = {},
        onClearError = {}
    )
}
