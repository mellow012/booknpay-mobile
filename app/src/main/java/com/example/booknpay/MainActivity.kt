package com.example.booknpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.booknpay.ui.auth.forgot_password.ForgotPasswordScreen
import com.example.booknpay.ui.auth.login.LoginScreen
import com.example.booknpay.ui.auth.register.RegisterScreen
import com.example.booknpay.ui.booking.BookingScreen
import com.example.booknpay.ui.booking.BookingViewModel
import com.example.booknpay.ui.components.PlaceholderScreen
import com.example.booknpay.ui.home.HomeScreen
import com.example.booknpay.ui.navigation.Screen
import com.example.booknpay.ui.navigation.bottomNavItems
import com.example.booknpay.ui.payment.PaymentScreen
import com.example.booknpay.ui.search.SearchScreen
import com.example.booknpay.ui.splash.SplashScreen
import com.example.booknpay.ui.theme.BooknpayTheme
import com.example.booknpay.ui.ticket.MyTicketsScreen
import com.example.booknpay.ui.ticket.TicketScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BooknpayTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { item.icon?.let { Icon(it, contentDescription = item.title) } },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                    onNavigateToAuth = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } } }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Register.route) { inclusive = true } } }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    navController = navController,
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToBookings = { navController.navigate(Screen.MyTickets.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onScheduleClick = { scheduleId -> navController.navigate(Screen.Booking.createRoute(scheduleId)) }
                )
            }
            composable(Screen.Search.route) {
                    SearchScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onScheduleClick = { scheduleId ->
                            navController.navigate(Screen.Booking.createRoute(scheduleId))
                        }
                    )
                }


            composable(Screen.Booking.route) { backStackEntry ->
                val scheduleId = backStackEntry.arguments?.getString("scheduleId")
                if (scheduleId != null) {
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.Booking.route)
                    }
                    val viewModel = hiltViewModel<BookingViewModel>(parentEntry)
                    BookingScreen(navController = navController, viewModel = viewModel)

                }

}

            composable(Screen.MyTickets.route) { MyTicketsScreen() }
            composable(Screen.Profile.route) { PlaceholderScreen(title = "Profile") { navController.popBackStack() } }
            composable(Screen.Notifications.route) { PlaceholderScreen(title = "Notifications") { navController.popBackStack() } }
            composable(Screen.Payment.route) { PaymentScreen(navController = navController) }
            composable(
                route = Screen.Ticket.route,
                arguments = listOf(navArgument("bookingReference") { type = androidx.navigation.NavType.StringType })
            ) {
                val bookingReference = it.arguments?.getString("bookingReference")
                TicketScreen(bookingReference = bookingReference) { navController.popBackStack() }
            }
        }
    }
}
