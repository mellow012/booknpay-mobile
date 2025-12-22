package com.example.booknpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.booknpay.ui.booking.BookingScreen
import com.example.booknpay.ui.booking.BookingViewModel
import com.example.booknpay.ui.booking.ConfirmationScreen
import com.example.booknpay.ui.home.HomeScreen
import com.example.booknpay.ui.navigation.Screen
import com.example.booknpay.ui.notifications.NotificationsScreen
import com.example.booknpay.ui.theme.BooknpayTheme
import com.example.booknpay.ui.ticket.MyTicketsScreen
import com.example.booknpay.ui.ticket.TicketScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BooknpayTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("booknpay") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val items = listOf(Screen.Home, Screen.MyTickets)

            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            // Nested Booking Flow
            navigation(startDestination = "booking_form", route = Screen.Booking.route) {
                composable("booking_form") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Booking.route) }
                    val viewModel = hiltViewModel<BookingViewModel>(parentEntry)
                    BookingScreen(navController, viewModel)
                }
                composable(Screen.Confirmation.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Booking.route) }
                    val viewModel = hiltViewModel<BookingViewModel>(parentEntry)
                    ConfirmationScreen(navController, viewModel)
                }
            }
            composable(Screen.MyTickets.route) { MyTicketsScreen() }
            composable(Screen.Notifications.route) { NotificationsScreen(navController) } 
            composable(
                route = Screen.Ticket.route,
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                    navArgument("passengerName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                TicketScreen(
                    bookingId = backStackEntry.arguments?.getString("bookingId"),
                    passengerName = backStackEntry.arguments?.getString("passengerName"),
                    onDone = { 
                        // Navigate to home and clear the back stack
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
