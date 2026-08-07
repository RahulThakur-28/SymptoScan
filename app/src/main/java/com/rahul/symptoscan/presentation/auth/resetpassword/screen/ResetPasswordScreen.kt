package com.rahul.symptoscan.presentation.auth.resetpassword.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.auth.common.AuthUiState
import com.rahul.symptoscan.presentation.auth.login.component.PasswordTextField
import com.rahul.symptoscan.presentation.auth.resetpassword.event.ResetPasswordEvent
import com.rahul.symptoscan.presentation.auth.resetpassword.viewmodel.ResetPasswordViewModel
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit,
    onUpdateSuccess: () -> Unit,
    viewModel: ResetPasswordViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onUpdateSuccess()
        } else if (uiState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = Dimens.PaddingExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
            
            Text(
                text = "Create a new secure password for your account.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
            
            PasswordTextField(
                value = state.password,
                onValueChange = { viewModel.onEvent(ResetPasswordEvent.PasswordChanged(it)) },
                label = "New Password",
                placeholder = "••••••••••",
                isVisible = state.isPasswordVisible,
                onToggleVisibility = { viewModel.onEvent(ResetPasswordEvent.TogglePasswordVisibility) },
                error = state.passwordError
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
            
            PasswordTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.onEvent(ResetPasswordEvent.ConfirmPasswordChanged(it)) },
                label = "Confirm New Password",
                placeholder = "••••••••••",
                isVisible = state.isConfirmPasswordVisible,
                onToggleVisibility = { viewModel.onEvent(ResetPasswordEvent.ToggleConfirmPasswordVisibility) },
                error = state.confirmPasswordError
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
            
            PrimaryButton(
                text = "Update Password",
                onClick = { viewModel.onEvent(ResetPasswordEvent.UpdateClicked) },
                enabled = state.isUpdateEnabled,
                isLoading = state.isLoading
            )
        }
    }
}
