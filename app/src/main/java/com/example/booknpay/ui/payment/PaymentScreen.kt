package com.example.booknpay.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknpay.ui.navigation.Screen

@Composable
fun PaymentScreen(
    navController: NavController,
    bookingId: String?,
    passengerName: String?
) {
    fun navigateToTicket() {
        if (bookingId != null && passengerName != null) {
            navController.navigate(Screen.Ticket.createRoute(bookingId, passengerName)) {
                // Pop the payment screen from the back stack to prevent returning to it
                popUpTo(navController.currentDestination!!.id) {
                    inclusive = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose a Payment Method")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navigateToTicket() }) {
            Text("Pay with Mobile")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navigateToTicket() }) {
            Text("Pay with Bank")
        }
    }
}
