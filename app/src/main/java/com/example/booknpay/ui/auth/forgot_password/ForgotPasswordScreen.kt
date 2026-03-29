package com.example.booknpay.ui.auth.forgot_password

// ─────────────────────────────────────────────────────────────────────────────
// ForgotPasswordViewModel.kt
// Put this in its own file: ui/auth/forgot_password/ForgotPasswordViewModel.kt
// ─────────────────────────────────────────────────────────────────────────────
// (Keeping both in this file only to ship them together — split before committing)

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.AuthResult
import com.example.booknpay.domain.repository.AuthRepository
import com.example.booknpay.domain.use_case.validation.ValidateEmailUseCase
import com.example.booknpay.domain.use_case.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────
data class ForgotPasswordState(
    val email:         String  = "",
    val emailError:    String? = null,
    val isLoading:     Boolean = false,
    val errorMessage:  String? = null,
    val isEmailSent:   Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateEmail:  ValidateEmailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onSendResetEmailClick() {
        val result = validateEmail.execute(_state.value.email)
        if (result is ValidationResult.Error) {
            _state.update { it.copy(emailError = result.message) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = authRepository.sendPasswordResetEmail(_state.value.email)) {
                is AuthResult.Success -> _state.update { it.copy(isLoading = false, isEmailSent = true) }
                is AuthResult.Error   -> _state.update { it.copy(isLoading = false, errorMessage = r.message) }
                is AuthResult.Loading -> Unit
            }
        }
    }

    fun clearError() { _state.update { it.copy(errorMessage = null) } }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsState()
    val focusManager  = LocalFocusManager.current

    if (state.isEmailSent) {
        AlertDialog(
            onDismissRequest = onNavigateBack,
            icon  = {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Email sent", textAlign = TextAlign.Center) },
            text  = {
                Text(
                    text      = "Password reset instructions have been sent to ${state.email}. Check your inbox.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick  = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Back to Sign In") }
            }
        )
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            icon  = { Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Error") },
            text  = { Text(state.errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            Spacer(modifier = Modifier.height(40.dp))

            // Lock icon in a tinted circle
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text       = "Forgot your password?",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text      = "Enter your email and we'll send you a link to reset your password.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value         = state.email,
                onValueChange = viewModel::onEmailChange,
                label         = { Text("Email address") },
                leadingIcon   = { Icon(Icons.Default.Email, null) },
                isError       = state.emailError != null,
                supportingText = { if (state.emailError != null) Text(state.emailError!!) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onSendResetEmailClick()
                    }
                ),
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                shape      = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick  = viewModel::onSendResetEmailClick,
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
                    Text("Send Reset Link", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateBack) {
                Text("Back to Sign In")
            }
        }
    }
}