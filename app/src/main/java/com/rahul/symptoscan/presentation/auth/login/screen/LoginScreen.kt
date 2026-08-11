package com.rahul.symptoscan.presentation.auth.login.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.login.component.*
import com.rahul.symptoscan.presentation.auth.login.event.LoginEvent
import com.rahul.symptoscan.presentation.auth.login.viewmodel.LoginViewModel
import com.rahul.symptoscan.ui.components.AppTextField
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.Dimens

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onEmailNotVerified: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoogleLogin: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    DisposableEffect(context) {
        val activity = context as? android.app.Activity
        val listener = androidx.core.util.Consumer<android.content.Intent> { intent ->
            intent.data?.let { uri ->
                if (uri.scheme == "symptoscan" && uri.host == "auth") {
                    viewModel.handleDeepLink(uri.toString())
                }
            }
        }
        
        // Handle current intent
        activity?.intent?.data?.let { uri ->
            if (uri.scheme == "symptoscan" && uri.host == "auth") {
                viewModel.handleDeepLink(uri.toString())
                // Clear intent to avoid duplicate processing
                activity.intent.data = null
            }
        }

        activity?.let {
            if (it is androidx.activity.ComponentActivity) {
                it.addOnNewIntentListener(listener)
            }
        }
        onDispose {
            activity?.let {
                if (it is androidx.activity.ComponentActivity) {
                    it.removeOnNewIntentListener(listener)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        visible = true

        viewModel.navigationEvent.collect { event ->
            when (event) {
                is LoginViewModel.LoginNavigation.NavigateToHome -> onLoginSuccess()
                is LoginViewModel.LoginNavigation.NavigateToVerification -> onEmailNotVerified()
                is LoginViewModel.LoginNavigation.NavigateToResetPassword -> onNavigateToResetPassword()
                is LoginViewModel.LoginNavigation.NavigateToProfile -> onNavigateToProfile()
            }
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
            }
            is AuthUiState.EmailNotVerified -> {
                onEmailNotVerified()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 40 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.PaddingExtraLarge, vertical = Dimens.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                HeaderSection()
                
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                
                AppTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                    label = "Email Address",
                    placeholder = "you@example.com",
                    error = state.emailError
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                PasswordTextField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                    label = "Password",
                    placeholder = "••••••••••",
                    isVisible = state.isPasswordVisible,
                    onToggleVisibility = { viewModel.onEvent(LoginEvent.TogglePasswordVisibility) },
                    error = state.passwordError
                )
                
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(onClick = onForgotPassword) {
                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height( Dimens.PaddingMedium))
                
                PrimaryButton(
                    text = "Sign In",
                    onClick = { viewModel.onEvent(LoginEvent.LoginClicked) },
                    enabled = state.isSignInEnabled,
                    isLoading = state.isLoading
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
                    TextButton(onClick = onRegisterClick) {
                        Text(
                            text = "Create Account",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                DividerWithText(text = "OR")
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                SocialButton(
                    text = "Continue with Google",
                    onClick = onGoogleLogin
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                
                SecurityCard()
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}