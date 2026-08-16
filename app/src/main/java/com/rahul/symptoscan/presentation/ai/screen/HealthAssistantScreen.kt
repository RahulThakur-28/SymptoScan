package com.rahul.symptoscan.presentation.ai.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.createSavedStateHandle
import com.rahul.symptoscan.core.di.Injection
import com.rahul.symptoscan.R
import com.rahul.symptoscan.domain.model.HealthMessage
import com.rahul.symptoscan.presentation.ai.component.HealthAssistantEmptyState
import com.rahul.symptoscan.presentation.ai.component.HealthAssistantInput
import com.rahul.symptoscan.presentation.ai.component.HealthMessageBubble
import com.rahul.symptoscan.presentation.ai.viewmodel.HealthAssistantViewModel
import com.rahul.symptoscan.presentation.home.component.HomeBottomNavigation
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.SymptoScanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAssistantScreen(
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: HealthAssistantViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HealthAssistantViewModel(
                    repository = Injection.healthAssistantRepository,
                    authRepository = Injection.authRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    ),
    showScaffold: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    if (showScaffold) {
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
    } else {
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
            onClearError = viewModel::clearError,
            showScaffold = false
        )
    }
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
    onClearError: () -> Unit,
    showScaffold: Boolean = true
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

    if (showScaffold) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HealthAssistantHeader(
                    onBackClick = onBackClick,
                    onHistoryClick = onHistoryClick,
                    onNewChatClick = onNewChatClick
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
            AssistantMainLayout(
                paddingValues = paddingValues,
                uiState = uiState,
                onSuggestionClick = onSuggestionClick,
                listState = listState,
                onInputChange = onInputChange,
                onSendClick = onSendClick
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            HealthAssistantHeader(
                onBackClick = onBackClick,
                onHistoryClick = onHistoryClick,
                onNewChatClick = onNewChatClick,
                elevation = 2.dp
            )
            AssistantMainLayout(
                paddingValues = PaddingValues(0.dp),
                uiState = uiState,
                onSuggestionClick = onSuggestionClick,
                listState = listState,
                onInputChange = onInputChange,
                onSendClick = onSendClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthAssistantHeader(
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNewChatClick: () -> Unit,
    elevation: androidx.compose.ui.unit.Dp = 0.dp
) {
    Surface(shadowElevation = elevation) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        color = BluePrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Column {
                        Text(
                            text = "AI Health Assistant",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Online",
                                fontSize = 11.sp,
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
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
    }
}

@Composable
private fun AssistantMainLayout(
    paddingValues: PaddingValues,
    uiState: com.rahul.symptoscan.presentation.ai.state.HealthAssistantUiState,
    onSuggestionClick: (String) -> Unit,
    listState: LazyListState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
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
