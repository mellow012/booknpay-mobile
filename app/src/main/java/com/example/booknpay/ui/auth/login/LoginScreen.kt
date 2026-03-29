package com.example.booknpay.ui.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booknpay.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister:       () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome:           () -> Unit,
    viewModel:                  LoginViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsState()
    val focusManager  = LocalFocusManager.current
    val context       = LocalContext.current
    val scope         = rememberCoroutineScope()

    LaunchedEffect(state.isLoginSuccessful) {
        if (state.isLoginSuccessful) onNavigateToHome()
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            icon  = { Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Sign in failed") },
            text  = { Text(state.errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        // ── Logo ──────────────────────────────────────────────────────────
        Image(
            painter            = painterResource(id = R.drawable.tibhukebus_logo),
            contentDescription = "TibhukeBus",
            modifier           = Modifier
                .width(220.dp)
                .aspectRatio(1.6f),
            contentScale       = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text  = "Your journey starts here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Email ─────────────────────────────────────────────────────────
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
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth(),
            shape      = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Password ──────────────────────────────────────────────────────
        OutlinedTextField(
            value         = state.password,
            onValueChange = viewModel::onPasswordChange,
            label         = { Text("Password") },
            leadingIcon   = { Icon(Icons.Default.Lock, null) },
            trailingIcon  = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (state.isPasswordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = if (state.isPasswordVisible) "Hide" else "Show"
                    )
                }
            },
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            isError       = state.passwordError != null,
            supportingText = { if (state.passwordError != null) Text(state.passwordError!!) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); viewModel.onLoginClick() }
            ),
            singleLine = true,
            modifier   = Modifier.fillMaxWidth(),
            shape      = MaterialTheme.shapes.medium
        )

        // ── Forgot password ───────────────────────────────────────────────
        TextButton(
            onClick  = onNavigateToForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot password?")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Sign in button ────────────────────────────────────────────────
        Button(
            onClick  = viewModel::onLoginClick,
            enabled  = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape    = MaterialTheme.shapes.large
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color    = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text("Sign In", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Divider ───────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text  = "  or  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Google sign-in ────────────────────────────────────────────────
        OutlinedButton(
            onClick = {
                scope.launch {
                    val result = com.example.booknpay.util.GoogleSignInHelper
                        .signInWithGoogle(context)
                    result
                        .onSuccess { viewModel.onGoogleSignInClick(it) }
                        .onFailure { viewModel.showError(it.message ?: "Google sign-in failed") }
                }
            },
            enabled  = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape    = MaterialTheme.shapes.large
        ) {
            // Google "G" colours — replace with painterResource(R.drawable.ic_google) once added
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Coloured G logo approximation using styled text
                Text(
                    text       = "G",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color(0xFF4285F4)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text  = "Continue with Google",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Register link ─────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text("Sign Up", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}