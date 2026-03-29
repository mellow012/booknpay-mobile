package com.example.booknpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.booknpay.data.model.Company
import com.example.booknpay.data.model.User
import com.example.booknpay.ui.auth.forgot_password.ForgotPasswordScreen
import com.example.booknpay.ui.auth.login.LoginScreen
import com.example.booknpay.ui.auth.register.RegisterScreen
import com.example.booknpay.ui.booking.BookingScreen
import com.example.booknpay.ui.booking.BookingViewModel
import com.example.booknpay.ui.company.CompanyDetailScreen
import com.example.booknpay.ui.company.CompanyScreen
import com.example.booknpay.ui.company.CompanyViewModel
import com.example.booknpay.ui.components.PlaceholderScreen
import com.example.booknpay.ui.home.HomeScreen
import com.example.booknpay.ui.home.HomeViewModel
import com.example.booknpay.ui.home.components.DiscoverScreen
import com.example.booknpay.ui.navigation.AppNavigationDrawer
import com.example.booknpay.ui.navigation.Screen
import com.example.booknpay.ui.navigation.bottomNavItems
import com.example.booknpay.ui.notifications.NotificationViewModel
import com.example.booknpay.ui.notifications.NotificationsScreen
import com.example.booknpay.ui.payment.PaymentScreen
import com.example.booknpay.ui.profile.ProfileScreen
import com.example.booknpay.ui.profile.ProfileState
import com.example.booknpay.ui.profile.ProfileViewModel
import com.example.booknpay.ui.routes.RouteSchedulesScreen
import com.example.booknpay.ui.routes.RouteScreen
import com.example.booknpay.ui.search.SearchScreen
import com.example.booknpay.ui.settings.SettingsScreen
import com.example.booknpay.ui.splash.OnboardingScreen
import com.example.booknpay.ui.splash.SplashScreen
import com.example.booknpay.ui.stations.StationsScreen
import com.example.booknpay.ui.theme.BooknpayTheme
import com.example.booknpay.ui.ticket.MyTicketsScreen
import com.example.booknpay.ui.ticket.TicketScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            BooknpayTheme(darkTheme = isDarkMode) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }


    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Placeholder - replace with real state later
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomBar,
        drawerContent = {
            AppNavigationDrawer(
                currentRoute = currentDestination?.route,
                user = profileState.user,
                unreadCount = unreadCount,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == item.route } == true

                            NavigationBarItem(
                                icon = { item.icon?.let { Icon(it, contentDescription = item.title) } },
                                label = { Text(item.title) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
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
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                // === SPLASH ===
                composable(Screen.Splash.route) {
                    SplashScreen(navController = navController, onNavigateToHome = {}, onNavigateToAuth = {})
                }

                // === ONBOARDING ===
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinish = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                // === AUTH ===
                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        onNavigateToLogin = { navController.popBackStack() },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
                }

                // === MAIN SCREENS ===
                composable(Screen.Home.route) {
                    HomeScreen(
                        // Drawer Logic
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },

                        // Top Bar actions
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },

                        // Quick Action Grid Mapping
                        onNavigateToSearch = {
                            // The "Book Bus" action now goes to our new Global Discover screen
                            navController.navigate(Screen.Search.route)
                        },
                        onNavigateToBookings = {
                            // "My Bookings" goes to the tickets list
                            navController.navigate(Screen.MyTickets.route)
                        },
                        onNavigateToRoutes = {
                            // "Schedules" or "Routes" goes to the route list
                            navController.navigate(Screen.Routes.route)
                        },

                        // List Item Clicks (Schedules/Popular Routes)
                        onScheduleClick = { scheduleId ->
                            navController.navigate(Screen.Booking.createRoute(scheduleId))
                        },
                        onRouteClick = { origin, destination ->
                            navController.navigate(Screen.RouteSchedules.createRoute(origin, destination))
                        },
                        onNavigateToParcels = {}
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

                composable(Screen.MyTickets.route) {
                    MyTicketsScreen(
                        onNavigateToPayment = { bookingId ->
                            navController.navigate("payment/$bookingId/0.0")
                        },
                        onViewTicket = { ref ->
                            navController.navigate(Screen.Ticket.createRoute(ref))
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateToBack = { navController.popBackStack() },
                        onNavigateToAuth = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }

                composable(Screen.Notifications.route) {
                    NotificationsScreen(onNavigateBack = { navController.popBackStack() })
                }

                // Booking Flow
                composable(
                    route = Screen.Booking.route,
                    arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bookingViewModel: BookingViewModel = hiltViewModel(backStackEntry)
                    BookingScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToTickets = {
                            navController.navigate(Screen.MyTickets.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        viewModel = bookingViewModel
                    )
                }

                composable(
                    route = Screen.RouteSchedules.route,
                    arguments = listOf(
                        navArgument("origin") { type = NavType.StringType },
                        navArgument("destination") { type = NavType.StringType }
                    )
                ) {
                    RouteSchedulesScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onBookSchedule = { scheduleId ->
                            navController.navigate(Screen.Booking.createRoute(scheduleId))
                        }
                    )
                }

                // Payment
                composable(
                    route = "payment/{bookingId}/{totalAmount}",
                    arguments = listOf(
                        navArgument("bookingId") { type = NavType.StringType },
                        navArgument("totalAmount") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    val totalAmount = backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0

                    PaymentScreen(
                        bookingId = bookingId,
                        totalAmount = totalAmount,
                        onSuccess = {
                            navController.navigate(Screen.MyTickets.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Ticket.route,
                    arguments = listOf(navArgument("bookingReference") { type = NavType.StringType })
                ) { backStackEntry ->
                    val ref = backStackEntry.arguments?.getString("bookingReference")
                    TicketScreen(
                        bookingReference = ref,
                        onDone = { navController.popBackStack() }
                    )
                }

                // Other screens
                composable(Screen.Discover.route) {
                    DiscoverScreen (

                    )
                }
                composable(Screen.Routes.route){
                    RouteScreen(
                        onRouteClick = { o, d ->
                            navController.navigate(Screen.RouteSchedules.createRoute(o, d))
                        },
                        onScheduleClick = { id ->
                            navController.navigate(Screen.Booking.createRoute(id))
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                    )
                }
                // 1. Bus Companies List
                composable(Screen.Companies.route) {
                    CompanyScreen(
                        onCompanyClick = { company ->
                            // Use the helper function from your Screen object
                            navController.navigate(Screen.CompanyDetails.createRoute(company.id))
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Inside your NavHost block
                composable(
                    route = Screen.CompanyDetails.route,
                    arguments = listOf(navArgument("companyId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("companyId") ?: ""

                    // 1. Get the ViewModel
                    val viewModel: CompanyViewModel = hiltViewModel()
                    // 2. Collect the list of companies
                    val companies by viewModel.companies.collectAsState()

                    // 3. Find the specific company that matches the ID
                    val selectedCompany = companies.find { it.id == id }

                    if (selectedCompany != null) {
                        // Now we pass the required 'company' and 'onBookRoute' parameters
                        CompanyDetailScreen(
                            company = selectedCompany,
                            onBookRoute = { serviceId ->
                                // Navigate to booking flow
                                navController.navigate(Screen.Booking.createRoute(serviceId))
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        // Show a loading state or error if the company isn't found yet
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }/*
// 3. Parcel Tracker
                composable(Screen.ParcelTracker.route) {
                    ParcelTrackerScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
*/
// 4. Bus Stations
                composable(Screen.Stations.route) {
                    StationsScreen(
                        onStationClick = { station ->
                            // Optionally navigate to a StationDetail or just show on map
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Help.route) {
                    PlaceholderScreen(
                        title = "Help & Support",
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}