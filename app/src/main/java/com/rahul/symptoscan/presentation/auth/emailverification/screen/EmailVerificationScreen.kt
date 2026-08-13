package com.rahul.symptoscan.presentation.auth.emailverification.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.core.utils.AuthValidator
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.emailverification.event.EmailVerificationEvent
import com.rahul.symptoscan.presentation.auth.emailverification.viewmodel.EmailVerificationViewModel
import com.rahul.symptoscan.ui.components.AppLogo
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: EmailVerificationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(EmailVerificationEvent.VerifyClicked)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onVerificationSuccess()
        } else if (uiState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.PaddingExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            AppLogo(
                size = 80.dp,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                iconColor = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            
            Text(
                text = "Check Your Email",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            
            Text(
                text = "We've sent a verification link to\n${AuthValidator.maskEmail(state.email)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
            
            Button(
                onClick = { 
                    val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                        addCategory(android.content.Intent.CATEGORY_APP_EMAIL)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback if no email app found
                        val mailIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("mailto:")
                        }
                        try {
                            context.startActivity(mailIntent)
                        } catch (e2: Exception) {
                            // Last resort fallback or notification
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(Dimens.CornerRadiusMedium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = "Open Email App", fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            
            PrimaryButton(
                text = "I've Verified",
                onClick = { viewModel.onEvent(EmailVerificationEvent.VerifyClicked) },
                isLoading = state.isLoading
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            
            TextButton(
                onClick = { viewModel.onEvent(EmailVerificationEvent.ResendEmailClicked) },
                enabled = state.canResend
            ) {
                Text(
                    text = if (state.canResend) "Resend Email" else "Resend available in ${state.resendCooldown}s",
                    color = if (state.canResend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Check your spam folder if you don't see it.",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
        }
    }
}
