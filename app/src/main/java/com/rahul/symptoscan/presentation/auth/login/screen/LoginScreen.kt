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
import com.rahul.symptoscan.presentation.auth.login.component.*
import com.rahul.symptoscan.presentation.auth.login.event.LoginEvent
import com.rahul.symptoscan.presentation.auth.login.viewmodel.LoginViewModel
import com.rahul.symptoscan.ui.components.AppTextField
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.Dimens

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoogleLogin: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 40 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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