package com.example.booknpay.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel ────────────────────────────────────────────────────────────────
data class SettingsState(
    val isDarkMode: Boolean = false,
    val pushNotificationsEnabled: Boolean = true,
    val emailReceiptsEnabled: Boolean = false,
    val biometricEnabled: Boolean = true,
    val currency: String = "MWK",
    val language: String = "English"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.isDarkMode.collect { dark ->
                _state.update { it.copy(isDarkMode = dark) }
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { themeRepository.toggleTheme(enabled) }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _state.update { it.copy(pushNotificationsEnabled = enabled) }
    }

    fun toggleEmailReceipts(enabled: Boolean) {
        _state.update { it.copy(emailReceiptsEnabled = enabled) }
    }

    fun toggleBiometric(enabled: Boolean) {
        _state.update { it.copy(biometricEnabled = enabled) }
    }

    fun setCurrency(currency: String) {
        _state.update { it.copy(currency = currency) }
    }

    fun setLanguage(language: String) {
        _state.update { it.copy(language = language) }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        OptionDialog(
            title = "Currency",
            options = listOf("MWK", "USD", "ZAR", "GBP"),
            selected = state.currency,
            onSelect = { viewModel.setCurrency(it); showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showLanguageDialog) {
        OptionDialog(
            title = "Language",
            options = listOf("English", "Chichewa"),
            selected = state.language,
            onSelect = { viewModel.setLanguage(it); showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance
            item {
                SettingsGroup(title = "Appearance") {
                    ToggleSettingItem(
                        icon = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        label = "Dark mode",
                        description = "Switch between light and dark themes",
                        checked = state.isDarkMode,
                        onCheckedChange = viewModel::toggleDarkMode
                    )
                }
            }

            // Notifications
            item {
                SettingsGroup(title = "Notifications") {
                    ToggleSettingItem(
                        icon = Icons.Default.Notifications,
                        label = "Push notifications",
                        description = "Bus delays, boarding alerts and offers",
                        checked = state.pushNotificationsEnabled,
                        onCheckedChange = viewModel::togglePushNotifications
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ToggleSettingItem(
                        icon = Icons.Default.Email,
                        label = "Email receipts",
                        description = "Monthly summary of your trips",
                        checked = state.emailReceiptsEnabled,
                        onCheckedChange = viewModel::toggleEmailReceipts
                    )
                }
            }

            // Security
            item {
                SettingsGroup(title = "Security") {
                    ToggleSettingItem(
                        icon = Icons.Default.Fingerprint,
                        label = "Biometric login",
                        description = "Use fingerprint or Face ID to sign in",
                        checked = state.biometricEnabled,
                        onCheckedChange = viewModel::toggleBiometric
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    NavigationSettingItem(
                        icon = Icons.Default.Lock,
                        label = "Change password",
                        description = "Update your account password",
                        onClick = onNavigateToForgotPassword
                    )
                }
            }

            // Regional
            item {
                SettingsGroup(title = "Regional") {
                    NavigationSettingItem(
                        icon = Icons.Default.Language,
                        label = "Language",
                        description = state.language,
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    NavigationSettingItem(
                        icon = Icons.Default.CurrencyExchange,
                        label = "Currency",
                        description = state.currency,
                        onClick = { showCurrencyDialog = true }
                    )
                }
            }

            // About
            item {
                SettingsGroup(title = "About") {
                    NavigationSettingItem(
                        icon = Icons.Default.PrivacyTip,
                        label = "Privacy policy",
                        description = "How we handle your data",
                        onClick = {}
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    NavigationSettingItem(
                        icon = Icons.Default.Info,
                        label = "App version",
                        description = "TibhukeBus v1.0.0",
                        onClick = {}
                    )
                }
            }
        }
    }
}

// ─── Settings group container ─────────────────────────────────────────────────
@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(content = content)
        }
    }
}

// ─── Toggle setting row ───────────────────────────────────────────────────────
@Composable
private fun ToggleSettingItem(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

// ─── Navigation setting row ───────────────────────────────────────────────────
@Composable
private fun NavigationSettingItem(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── Generic option dialog ────────────────────────────────────────────────────
@Composable
private fun OptionDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) }
                        )
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}