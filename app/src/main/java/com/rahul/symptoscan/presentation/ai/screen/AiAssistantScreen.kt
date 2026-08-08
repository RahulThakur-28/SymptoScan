package com.rahul.symptoscan.presentation.ai.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.rahul.symptoscan.domain.model.AssessmentSummary
import com.rahul.symptoscan.presentation.ai.model.ChatMessage
import com.rahul.symptoscan.presentation.ai.model.MessageRole
import com.rahul.symptoscan.presentation.ai.state.AiAssistantUiState
import com.rahul.symptoscan.presentation.ai.viewmodel.AiAssistantViewModel
import com.rahul.symptoscan.presentation.home.component.HomeBottomNavigation
import com.rahul.symptoscan.ui.components.MedicalDisclaimer
import com.rahul.symptoscan.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onNavigate: (String) -> Unit,
    viewModel: AiAssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            HomeBottomNavigation(
                currentRoute = "ai",
                onNavigate = onNavigate
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BluePrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = BluePrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("AI Health Assistant", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Online • Medical AI", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear conversation", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                viewModel.onDeleteConversationClicked()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Assessment Context Banner
                uiState.latestAssessmentContext?.let { assessment ->
                    AssessmentContextBanner(assessment = assessment)
                }

                ChatList(
                    messages = uiState.messages,
                    isTyping = uiState.isTyping,
                    suggestedQuestions = uiState.suggestedQuestions,
                    onSuggestedQuestionClick = { viewModel.onSuggestedQuestionClicked(it) },
                    modifier = Modifier.weight(1f)
                )

                ChatInputBar(
                    text = uiState.inputText,
                    onTextChange = { viewModel.onMessageChanged(it) },
                    onSend = { viewModel.onSendMessage() },
                    enabled = !uiState.isTyping
                )
            }

            if (uiState.showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { viewModel.onDismissDeleteDialog() },
                    title = { Text("Delete conversation?") },
                    text = { Text("This will permanently remove this conversation. This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.onConfirmDelete() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AssessmentContextBanner(assessment: AssessmentSummary) {
    Surface(
        color = BluePrimary.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Responding based on your latest assessment • Risk Score ${assessment.score}",
                fontSize = 12.sp,
                color = BluePrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChatList(
    messages: List<ChatMessage>,
    isTyping: Boolean,
    suggestedQuestions: List<String>,
    onSuggestedQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty() || isTyping) {
            coroutineScope.launch {
                listState.animateScrollToItem(if (isTyping) messages.size else messages.size - 1)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (messages.isEmpty()) {
            item {
                EmptyChatState()
            }
            items(suggestedQuestions) { question ->
                SuggestedQuestionCard(text = question, onClick = { onSuggestedQuestionClick(question) })
            }
        } else {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }

            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isUser) BluePrimary else Color.White
    val contentColor = if (isUser) Color.White else TextDark
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BluePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = if (isUser) 0.dp else 1.dp,
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontSize = 14.sp,
                    color = contentColor,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun SuggestedQuestionCard(text: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = BluePrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyChatState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BluePrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("How can I help you today?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "I can explain your assessment results and provide general health information.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun TypingIndicator() {
    Row(verticalAlignment = Alignment.Bottom) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BluePrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MedicalDisclaimer(modifier = Modifier.padding(bottom = 12.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about your symptoms...", fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = BluePrimary.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            FloatingActionButton(
                onClick = { if (enabled && text.isNotBlank()) onSend() },
                containerColor = if (text.isNotBlank() && enabled) BluePrimary else Color.LightGray.copy(alpha = 0.3f),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
            }
        }
    }
}
