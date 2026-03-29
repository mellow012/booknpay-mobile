package com.example.booknpay.ui.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome:  () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsState()
    val focusManager  = LocalFocusManager.current

    LaunchedEffect(state.isRegistrationSuccessful) {
        if (state.isRegistrationSuccessful) onNavigateToHome()
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            icon  = { Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Registration failed") },
            text  = { Text(state.errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = "Create account",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = "Travel with ease and comfort",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Name row ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value         = state.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    label         = { Text("First name") },
                    leadingIcon   = { Icon(Icons.Default.Person, null) },
                    isError       = state.firstNameError != null,
                    supportingText = { if (state.firstNameError != null) Text(state.firstNameError!!) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                    singleLine = true,
                    modifier   = Modifier.weight(1f),
                    shape      = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value         = state.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    label         = { Text("Last name") },
                    isError       = state.lastNameError != null,
                    supportingText = { if (state.lastNameError != null) Text(state.lastNameError!!) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier   = Modifier.weight(1f),
                    shape      = MaterialTheme.shapes.medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Email ─────────────────────────────────────────────────────
            OutlinedTextField(
                value         = state.email,
                onValueChange = viewModel::onEmailChange,
                label         = { Text("Email") },
                leadingIcon   = { Icon(Icons.Default.Email, null) },
                isError       = state.emailError != null,
                supportingText = { if (state.emailError != null) Text(state.emailError!!) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                shape      = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Phone ─────────────────────────────────────────────────────
            OutlinedTextField(
                value         = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label         = { Text("Phone number") },
                leadingIcon   = { Icon(Icons.Default.Phone, null) },
                placeholder   = { Text("+265 991 234 567") },
                isError       = state.phoneError != null,
                supportingText = {
                    Text(
                        if (state.phoneError != null) state.phoneError!!
                        else "Airtel or TNM number (used for payments)"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                shape      = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Password ──────────────────────────────────────────────────
            OutlinedTextField(
                value         = state.password,
                onValueChange = viewModel::onPasswordChange,
                label         = { Text("Password") },
                leadingIcon   = { Icon(Icons.Default.Lock, null) },
                trailingIcon  = {
                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                        Icon(
                            if (state.isPasswordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                isError       = state.passwordError != null,
                supportingText = { if (state.passwordError != null) Text(state.passwordError!!) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                shape      = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Confirm password ──────────────────────────────────────────
            OutlinedTextField(
                value         = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label         = { Text("Confirm password") },
                leadingIcon   = { Icon(Icons.Default.Lock, null) },
                trailingIcon  = {
                    IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                        Icon(
                            if (state.isConfirmPasswordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                isError       = state.confirmPasswordError != null,
                supportingText = { if (state.confirmPasswordError != null) Text(state.confirmPasswordError!!) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus(); viewModel.onRegisterClick() }
                ),
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                shape      = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Register button ───────────────────────────────────────────
            Button(
                onClick  = viewModel::onRegisterClick,
                enabled  = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = MaterialTheme.shapes.large
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Create Account", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}