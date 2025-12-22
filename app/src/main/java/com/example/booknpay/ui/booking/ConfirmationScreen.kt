package com.example.booknpay.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknpay.ui.components.LoadingOverlay
import com.example.booknpay.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    navController: NavController,
    viewModel: BookingViewModel // No longer creates its own ViewModel
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.bookingResult) {
        state.bookingResult?.let {
            navController.navigate(Screen.Ticket.createRoute(it.bookingId, it.passengerName)) {
                // Clear the booking flow from the back stack
                popUpTo(Screen.Booking.route) { inclusive = true }
            }
            viewModel.onEvent(BookingEvent.BookingFlowCompleted)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Booking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.selectedDeparture?.let {
                    Text("Route: ${it.originCity} to ${it.destinationCity}")
                    Text("Operator: ${it.departure.operatorName}")
                    Text("Price: ${it.departure.basePriceMWK} MWK")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Passenger: ${state.passengerName}")
                Text("Age: ${state.passengerAge}")
                Text("Sex: ${state.passengerSex}")
                Text("Phone: ${state.passengerPhone}")
                Text("Number of Passengers: ${state.numberOfPassengers}")
                Text("Payment Method: ${state.paymentMethod}")

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = { viewModel.onEvent(BookingEvent.BookTicket) }) {
                    Text("Confirm and Pay")
                }
            }

            if (state.isLoading) {
                LoadingOverlay(message = "Processing your booking...")
            }
        }
    }
}
