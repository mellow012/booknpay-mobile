package com.example.booknpay.ui.ticket

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MyTicketsScreen(
    viewModel: MyTicketsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onEvent(MyTicketsEvent.SearchTickets(it)) },
            label = { Text("Search by Passenger Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text("My Booked Tickets", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(state.tickets) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${it.originCity} to ${it.destinationCity}", fontWeight = FontWeight.Bold)
                        Text("Operator: ${it.operatorName}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Passenger: ${it.passengerName}")
                        Text("Booking ID: ${it.bookingId}")
                        Text("Status: ${it.paymentStatus}", color = if (it.paymentStatus == "CONFIRMED") androidx.compose.ui.graphics.Color.Green else androidx.compose.ui.graphics.Color.Red)
                    }
                }
            }
        }
    }
}
