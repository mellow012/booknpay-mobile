package com.example.booknpay.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.booknpay.ui.booking.components.ConfirmationScreen
import com.example.booknpay.ui.booking.components.PassengerDetailsScreen
import com.example.booknpay.ui.components.ErrorState
import com.example.booknpay.ui.components.LoadingOverlay

// ── No NavController — navigation via lambdas ─────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onNavigateBack:      () -> Unit,
    onNavigateToTickets: () -> Unit,
    viewModel:           BookingViewModel
) {
    val state by viewModel.state.collectAsState()

    // ── Success dialog ─────────────────────────────────────────────────────
    if (state.bookingSuccess) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Request submitted!", textAlign = TextAlign.Center) },
            text  = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text      = "Your booking request has been sent to the operator.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text       = "Ref: ${state.bookingReference}",
                            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text      = "You'll be notified once the operator reviews your request. Payment required after approval.",
                        style     = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick  = onNavigateToTickets,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("View my bookings") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Back to home") }
            }
        )
    }

    // ── Error dialog ───────────────────────────────────────────────────────
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            icon  = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Something went wrong") },
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
                        when (state.currentStep) {
                            BookingStep.SEAT_SELECTION    -> "Select seats"
                            BookingStep.PASSENGER_DETAILS -> "Passenger details"
                            BookingStep.CONFIRMATION      -> "Confirm booking"
                            BookingStep.PAYMENT           -> "Payment"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.currentStep == BookingStep.SEAT_SELECTION) onNavigateBack()
                        else viewModel.goBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Step indicator ─────────────────────────────────────────────
            StepIndicator(currentStep = state.currentStep)

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading -> LoadingOverlay("Processing your request…")

                    state.scheduleDetails == null -> ErrorState(
                        message = "Schedule not found",
                        onRetry = onNavigateBack
                    )

                    else -> when (state.currentStep) {
                        BookingStep.SEAT_SELECTION -> SeatSelectionScreen(
                            state       = state,
                            onSeatClick = viewModel::toggleSeatSelection,
                            onProceed   = viewModel::proceedToPassengerDetails
                        )
                        BookingStep.PASSENGER_DETAILS -> PassengerDetailsScreen(
                            state            = state,
                            onPassengerUpdate = viewModel::updatePassengerDetail,
                            onProceed        = viewModel::proceedToConfirmation,
                            onBack           = viewModel::goBack
                        )
                        BookingStep.CONFIRMATION -> ConfirmationScreen(
                            state                  = state,
                            onSubmitBookingRequest = viewModel::submitBookingRequest,
                            onBack                 = viewModel::goBack
                        )
                        BookingStep.PAYMENT -> Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Payment screen — wired after operator approval")
                        }
                    }
                }
            }
        }
    }
}

// ── Step indicator bar ────────────────────────────────────────────────────────
@Composable
private fun StepIndicator(currentStep: BookingStep) {
    val steps = listOf("Seats", "Details", "Confirm")
    val currentIndex = when (currentStep) {
        BookingStep.SEAT_SELECTION    -> 0
        BookingStep.PASSENGER_DETAILS -> 1
        BookingStep.CONFIRMATION, BookingStep.PAYMENT -> 2
    }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val done   = index < currentIndex
            val active = index == currentIndex

            // Circle
            Box(
                modifier         = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            done   -> MaterialTheme.colorScheme.primary
                            active -> MaterialTheme.colorScheme.primary
                            else   -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint     = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text  = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (active) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Label
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
            )

            // Connector
            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier  = Modifier
                        .width(32.dp)
                        .padding(horizontal = 6.dp),
                    thickness = 1.5.dp,
                    color     = if (done) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}