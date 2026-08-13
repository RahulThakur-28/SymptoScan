package com.rahul.symptoscan.presentation.auth.register.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import com.rahul.symptoscan.presentation.auth.login.component.HeaderSection
import com.rahul.symptoscan.presentation.auth.login.component.PasswordTextField
import com.rahul.symptoscan.presentation.auth.login.component.SecurityCard
import com.rahul.symptoscan.presentation.auth.register.component.PasswordRequirementsSection
import com.rahul.symptoscan.presentation.auth.register.event.RegisterEvent
import com.rahul.symptoscan.presentation.auth.register.viewmodel.RegisterViewModel
import com.rahul.symptoscan.ui.components.AppTextField
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.Dimens

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToVerification: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onNavigateToVerification()
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
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
                HeaderSection(
                    title = "Create Account",
                    subtitle = "Join SymptoScan for personal health guidance"
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                
                AppTextField(
                    value = state.fullName,
                    onValueChange = { viewModel.onEvent(RegisterEvent.FullNameChanged(it)) },
                    label = "Full Name",
                    placeholder = "John Doe",
                    error = state.fullNameError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                AppTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(RegisterEvent.EmailChanged(it)) },
                    label = "Email Address",
                    placeholder = "you@example.com",
                    error = state.emailError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                PasswordTextField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
                    label = "Password",
                    placeholder = "••••••••••",
                    isVisible = state.isPasswordVisible,
                    onToggleVisibility = { viewModel.onEvent(RegisterEvent.TogglePasswordVisibility) },
                    error = state.passwordError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordRequirementsSection(password = state.password)
                }
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                PasswordTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                    label = "Confirm Password",
                    placeholder = "••••••••••",
                    isVisible = state.isConfirmPasswordVisible,
                    onToggleVisibility = { viewModel.onEvent(RegisterEvent.ToggleConfirmPasswordVisibility) },
                    error = state.confirmPasswordError
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                
                PrimaryButton(
                    text = "Continue",
                    onClick = { 
                        viewModel.onEvent(RegisterEvent.RegisterClicked)
                    },
                    enabled = state.isRegisterEnabled,
                    isLoading = state.isLoading
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 14.sp
                    )
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Sign In",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
                
                SecurityCard()
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}