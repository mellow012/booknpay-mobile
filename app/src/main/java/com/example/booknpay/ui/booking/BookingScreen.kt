package com.example.booknpay.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknpay.ui.booking.components.ConfirmationScreen
import com.example.booknpay.ui.booking.components.PassengerDetailsScreen
import com.example.booknpay.ui.components.ErrorState
import com.example.booknpay.ui.components.LoadingOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController,
    viewModel: BookingViewModel
) {
    val state by viewModel.state.collectAsState()

    // Success Dialog
    if (state.bookingSuccess) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Request Submitted!",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your booking request has been sent to the operator.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reference: ${state.bookingReference}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You'll receive a notification once the operator reviews your request. " +
                                "Payment will be required only after approval.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Navigate to My Tickets screen
                        navController.navigate("my_tickets") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View My Bookings")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            }
        )
    }

    // Error dialog
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Error") },
            text = { Text(state.error ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.currentStep) {
                            BookingStep.SEAT_SELECTION -> "Select Seats"
                            BookingStep.PASSENGER_DETAILS -> "Passenger Details"
                            BookingStep.CONFIRMATION -> "Confirm Booking"
                            BookingStep.PAYMENT -> "Payment"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.currentStep == BookingStep.SEAT_SELECTION) {
                                navController.popBackStack()
                            } else {
                                viewModel.goBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                state.isLoading -> {
                    LoadingOverlay("Processing your request...")
                }
                state.scheduleDetails == null -> {
                    ErrorState(
                        message = "Schedule not found",
                        onRetry = { navController.popBackStack() }
                    )
                }
                else -> {
                    when (state.currentStep) {
                        BookingStep.SEAT_SELECTION -> {
                            SeatSelectionScreen(
                                state = state,
                                onSeatClick = viewModel::toggleSeatSelection,
                                onProceed = viewModel::proceedToPassengerDetails
                            )
                        }
                        BookingStep.PASSENGER_DETAILS -> {
                            PassengerDetailsScreen(
                                state = state,
                                onPassengerUpdate = viewModel::updatePassengerDetail,
                                onProceed = viewModel::proceedToConfirmation,
                                onBack = viewModel::goBack
                            )
                        }
                        BookingStep.CONFIRMATION -> {
                            ConfirmationScreen(
                                state = state,
                                onSubmitBookingRequest = viewModel::submitBookingRequest,
                                onBack = viewModel::goBack
                            )
                        }
                        BookingStep.PAYMENT -> {
                            // This will be shown after operator approves
                            Text("Payment screen - coming after approval!")
                        }
                    }
                }
            }
        }
    }
}