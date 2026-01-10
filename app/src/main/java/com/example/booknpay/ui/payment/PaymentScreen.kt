package com.example.booknpay.ui.payment

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun PaymentScreen(navController: NavController){
    val navController = rememberNavController()

    Column(){
        Text("Payment")
    }
}