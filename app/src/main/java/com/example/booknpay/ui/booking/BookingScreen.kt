package com.example.booknpay.ui.booking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.booknpay.data.model.DepartureDetails
import com.example.booknpay.ui.components.LoadingOverlay

@Composable
fun BookingScreen(
    navController: NavController,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BookingForm(state, viewModel::onEvent) { 
                viewModel.onEvent(BookingEvent.ValidateForm)
            }

            state.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = Color.Red)
            }
        }

        if (state.isLoading) {
            LoadingOverlay(message = "Loading departures...")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingForm(
    state: BookingState,
    onEvent: (BookingEvent) -> Unit,
    onProceedToConfirmation: () -> Unit
) {
    val cities = state.departures.map { it.originCity }.distinct()

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = state.searchQuery,
            onValueChange = { onEvent(BookingEvent.SearchDepartures(it, state.selectedOrigin, state.selectedDestination)) },
            label = { Text("Search by Operator") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            SearchableDropdown(label = "Origin", options = cities, selected = state.selectedOrigin) { origin ->
                onEvent(BookingEvent.SearchDepartures(state.searchQuery, origin, state.selectedDestination))
            }
            SearchableDropdown(label = "Destination", options = cities, selected = state.selectedDestination) { destination ->
                onEvent(BookingEvent.SearchDepartures(state.searchQuery, state.selectedOrigin, destination))
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(state.departures) {
                DepartureItem(departureDetails = it, state.selectedDeparture, onEvent)
            }
        }

        state.selectedDeparture?.let { 
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = state.passengerName,
                onValueChange = { onEvent(BookingEvent.PassengerNameChanged(it)) },
                label = { Text("Full Name") },
                isError = state.passengerNameError != null,
                modifier = Modifier.fillMaxWidth()
            )
            state.passengerNameError?.let { Text(it, color = Color.Red) }

            TextField(
                value = state.passengerAge,
                onValueChange = { onEvent(BookingEvent.PassengerAgeChanged(it)) },
                label = { Text("Age") },
                isError = state.passengerAgeError != null,
                modifier = Modifier.fillMaxWidth()
            )
            state.passengerAgeError?.let { Text(it, color = Color.Red) }

            // Gender Radio Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sex:")
                RadioButton(
                    selected = state.passengerSex == "Male",
                    onClick = { onEvent(BookingEvent.PassengerSexChanged("Male")) }
                )
                Text("Male")
                RadioButton(
                    selected = state.passengerSex == "Female",
                    onClick = { onEvent(BookingEvent.PassengerSexChanged("Female")) }
                )
                Text("Female")
            }

            TextField(
                value = state.passengerPhone,
                onValueChange = { onEvent(BookingEvent.PassengerPhoneChanged(it)) },
                label = { Text("Phone Number") },
                isError = state.passengerPhoneError != null,
                modifier = Modifier.fillMaxWidth()
            )
            state.passengerPhoneError?.let { Text(it, color = Color.Red) }

            // Payment Method
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Payment Method:")
                RadioButton(
                    selected = state.paymentMethod == "Mobile",
                    onClick = { onEvent(BookingEvent.PaymentMethodChanged("Mobile")) }
                )
                Text("Mobile")
                RadioButton(
                    selected = state.paymentMethod == "Bank",
                    onClick = { onEvent(BookingEvent.PaymentMethodChanged("Bank")) }
                )
                Text("Bank")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onProceedToConfirmation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Proceed to Confirmation")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdown(label: String, options: List<String>, selected: String?, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { 
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DepartureItem(departureDetails: DepartureDetails, selectedDeparture: DepartureDetails?, onEvent: (BookingEvent) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEvent(BookingEvent.SelectDeparture(departureDetails)) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.DirectionsBus, contentDescription = "Departure")
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text("${departureDetails.originCity} to ${departureDetails.destinationCity}", fontWeight = FontWeight.Bold)
                Text("Operator: ${departureDetails.departure.operatorName}")
                Text("Price: ${departureDetails.departure.basePriceMWK} MWK")
                val seatsAvailable = departureDetails.departure.seatsAllocatedToSafePass - departureDetails.departure.seatsSoldBySafePass
                Text("Seats Available: $seatsAvailable")
            }
            RadioButton(
                selected = departureDetails.departure.departureId == selectedDeparture?.departure?.departureId,
                onClick = { onEvent(BookingEvent.SelectDeparture(departureDetails)) }
            )
        }
    }
}
