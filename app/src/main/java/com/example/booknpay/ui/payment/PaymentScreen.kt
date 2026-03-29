package com.example.booknpay.ui.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Payment method enum ──────────────────────────────────────────────────────
enum class MobileProvider { AIRTEL, TNM, CASH }

// ─── ViewModel state ──────────────────────────────────────────────────────────
data class PaymentState(
    val bookingId:      String  = "",
    val totalAmount:    Double  = 0.0,
    val phone:          String  = "",
    val phoneError:     String? = null,
    val provider:       MobileProvider = MobileProvider.AIRTEL,
    val step:           PaymentStep = PaymentStep.SELECT_METHOD,
    val isLoading:      Boolean = false,
    val pollAttempts:   Int     = 0,
    val error:          String? = null,
    val isSuccess:      Boolean = false
)

enum class PaymentStep { SELECT_METHOD, ENTER_PHONE, PENDING_STK, SUCCESS, CASH_CONFIRM }

// ─── ViewModel (wires to PayChangu API — implement initiateMobileMoney) ────────
@HiltViewModel
class PaymentViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    fun loadBooking(bookingId: String, totalAmount: Double) {
        _state.update { it.copy(bookingId = bookingId, totalAmount = totalAmount) }
    }

    fun onProviderSelected(provider: MobileProvider) {
        _state.update {
            it.copy(
                provider = provider,
                step     = if (provider == MobileProvider.CASH) PaymentStep.CASH_CONFIRM
                else PaymentStep.ENTER_PHONE
            )
        }
    }

    fun onPhoneChange(phone: String) {
        _state.update { it.copy(phone = phone, phoneError = null) }
    }

    fun submitMobileMoney() {
        val phone = _state.value.phone.trim()
        if (phone.length < 9) {
            _state.update { it.copy(phoneError = "Enter a valid phone number") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, step = PaymentStep.PENDING_STK) }
            // TODO: call your PayChangu API here:
            //   paychanguService.initiateSTKPush(phone, amount, bookingId)
            // Then poll Firestore booking doc for paymentStatus == "paid"
            pollForPayment()
        }
    }

    fun confirmCash() {
        // Cash onboarding — mark booking as "cash_pending" in Firestore
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(800) // simulate write
            _state.update { it.copy(isLoading = false, isSuccess = true, step = PaymentStep.SUCCESS) }
        }
    }

    private suspend fun pollForPayment() {
        // Poll up to 20 times (60 seconds) waiting for Firestore update
        repeat(20) { attempt ->
            delay(3000)
            _state.update { it.copy(pollAttempts = attempt + 1) }
            // TODO: check Firestore booking doc paymentStatus
            // val booking = bookingRepository.getBookingById(_state.value.bookingId)
            // if (booking?.paymentStatus == "paid") {
            //     _state.update { it.copy(isSuccess = true, step = PaymentStep.SUCCESS, isLoading = false) }
            //     return
            // }
        }
        // Timeout
        _state.update {
            it.copy(
                isLoading = false,
                step      = PaymentStep.ENTER_PHONE,
                error     = "Payment not confirmed. Check your phone and try again."
            )
        }
    }

    fun goBack() {
        _state.update {
            it.copy(
                step    = PaymentStep.SELECT_METHOD,
                error   = null,
                phone   = "",
                phoneError = null
            )
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    bookingId:   String = "",
    totalAmount: Double = 0.0,
    onSuccess:   () -> Unit = {},
    onBack:      () -> Unit = {},
    viewModel:   PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(bookingId) {
        viewModel.loadBooking(bookingId, totalAmount)
    }
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            icon  = { Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Payment issue") },
            text  = { Text(state.error ?: "") },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.step) {
                            PaymentStep.SELECT_METHOD  -> "Payment"
                            PaymentStep.ENTER_PHONE    -> "Enter phone number"
                            PaymentStep.PENDING_STK    -> "Waiting for payment"
                            PaymentStep.CASH_CONFIRM   -> "Cash on boarding"
                            PaymentStep.SUCCESS        -> "Payment confirmed"
                        }
                    )
                },
                navigationIcon = {
                    if (state.step == PaymentStep.SELECT_METHOD ||
                        state.step == PaymentStep.ENTER_PHONE ||
                        state.step == PaymentStep.CASH_CONFIRM) {
                        IconButton(onClick = {
                            if (state.step == PaymentStep.SELECT_METHOD) onBack()
                            else viewModel.goBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            when (state.step) {
                PaymentStep.SELECT_METHOD -> SelectMethodStep(
                    totalAmount      = state.totalAmount,
                    onProviderSelect = viewModel::onProviderSelected
                )
                PaymentStep.ENTER_PHONE -> EnterPhoneStep(
                    provider   = state.provider,
                    phone      = state.phone,
                    phoneError = state.phoneError,
                    amount     = state.totalAmount,
                    isLoading  = state.isLoading,
                    onPhoneChange = viewModel::onPhoneChange,
                    onSubmit   = viewModel::submitMobileMoney
                )
                PaymentStep.PENDING_STK -> PendingSTKStep(
                    provider     = state.provider,
                    phone        = state.phone,
                    pollAttempts = state.pollAttempts
                )
                PaymentStep.CASH_CONFIRM -> CashConfirmStep(
                    amount    = state.totalAmount,
                    isLoading = state.isLoading,
                    onConfirm = viewModel::confirmCash
                )
                PaymentStep.SUCCESS -> { /* LaunchedEffect navigates away */ }
            }
        }
    }
}

// ─── Step 1: Select method ────────────────────────────────────────────────────
@Composable
private fun SelectMethodStep(
    totalAmount:      Double,
    onProviderSelect: (MobileProvider) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(16.dp))

        // Amount display
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = MaterialTheme.shapes.large,
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = "Amount to pay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = "MWK ${"%.0f".format(totalAmount)}",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text  = "Choose payment method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Airtel Money
        ProviderCard(
            emoji       = "🔴",
            name        = "Airtel Money",
            description = "Pay with Airtel Mobile Money",
            color       = Color(0xFFEF233C),
            onClick     = { onProviderSelect(MobileProvider.AIRTEL) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // TNM Mpamba
        ProviderCard(
            emoji       = "🟡",
            name        = "TNM Mpamba",
            description = "Pay with TNM Mpamba",
            color       = Color(0xFFFFC107),
            onClick     = { onProviderSelect(MobileProvider.TNM) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Cash onboarding
        ProviderCard(
            emoji       = "💵",
            name        = "Cash on boarding",
            description = "Pay the conductor when you board",
            color       = MaterialTheme.colorScheme.secondary,
            onClick     = { onProviderSelect(MobileProvider.CASH) }
        )
    }
}

@Composable
private fun ProviderCard(
    emoji:       String,
    name:        String,
    description: String,
    color:       Color,
    onClick:     () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Step 2: Enter phone ──────────────────────────────────────────────────────
@Composable
private fun EnterPhoneStep(
    provider:     MobileProvider,
    phone:        String,
    phoneError:   String?,
    amount:       Double,
    isLoading:    Boolean,
    onPhoneChange: (String) -> Unit,
    onSubmit:     () -> Unit
) {
    val hint = if (provider == MobileProvider.AIRTEL) "+265 999 XXX XXX" else "+265 888 XXX XXX"
    val providerName = if (provider == MobileProvider.AIRTEL) "Airtel Money" else "TNM Mpamba"

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text      = "Enter your $providerName number to receive the payment prompt.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value         = phone,
            onValueChange = onPhoneChange,
            label         = { Text("Phone number") },
            placeholder   = { Text(hint) },
            leadingIcon   = { Icon(Icons.Default.Phone, null) },
            isError       = phoneError != null,
            supportingText = { if (phoneError != null) Text(phoneError) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Amount reminder
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier          = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint     = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text  = "MWK ${"%.0f".format(amount)} will be charged from this number",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick  = onSubmit,
            enabled  = !isLoading && phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = MaterialTheme.shapes.large
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    color       = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text("Send payment prompt", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ─── Step 3: Waiting for STK push ─────────────────────────────────────────────
@Composable
private fun PendingSTKStep(
    provider:     MobileProvider,
    phone:        String,
    pollAttempts: Int
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.08f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation  = androidx.compose.animation.core.tween(700),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier            = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing phone icon
        Box(
            modifier         = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .then(
                    Modifier.size((100 * scale).dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PhoneAndroid,
                null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text       = "Check your phone",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        val providerName = if (provider == MobileProvider.AIRTEL) "Airtel Money" else "TNM Mpamba"
        Text(
            text      = "A $providerName payment prompt has been sent to $phone.\nEnter your PIN to complete the payment.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        CircularProgressIndicator(strokeWidth = 3.dp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text  = "Checking payment status… ($pollAttempts/20)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Cash on boarding confirmation ────────────────────────────────────────────
@Composable
private fun CashConfirmStep(
    amount:    Double,
    isLoading: Boolean,
    onConfirm: () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.MonetizationOn,
            null,
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text       = "Pay on boarding",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text      = "Pay MWK ${"%.0f".format(amount)} in cash directly to the conductor when you board the bus.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Warning card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape    = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier          = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Warning,
                    null,
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text  = "Your seat is reserved but NOT guaranteed until payment is made. Arrive early.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick  = onConfirm,
            enabled  = !isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = MaterialTheme.shapes.large
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    color       = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text("Confirm cash payment", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}