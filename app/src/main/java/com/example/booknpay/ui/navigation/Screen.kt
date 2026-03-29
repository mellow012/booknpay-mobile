package com.example.booknpay.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A sealed class to define all app screens and their properties.
 * This provides a single source of truth for navigation.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {

    // --- Screens without a permanent place in navigation chrome ---
    object Splash : Screen("splash", "Splash", null)
    object Onboarding: Screen(route = "onboarding",title="Onboarding", icon =null)
    object Confirmation : Screen("confirmation", "Confirmation", null)
    object Payment : Screen("payment", "Payment", null)
    object Ticket : Screen("ticket/{bookingReference}", "Ticket", null) {
        fun createRoute(bookingReference: String) = "ticket/$bookingReference"
    }

    // --- Authentication Screens ---
    object Login : Screen("login", "Login", null)
    object Register : Screen("register", "Register", null)
    object ForgotPassword : Screen("forgot_password", "Forgot Password", null)

    // --- Main Screens (Bottom Navigation) ---
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object MyTickets : Screen("my_tickets", "My Tickets", Icons.AutoMirrored.Filled.LibraryBooks)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)

    object ScheduleDetails : Screen("schedule_details/{scheduleId}", "Schedule Details", null)
    object BookingDetails : Screen("booking_details/{bookingId}", "Booking Details", null)

    // --- Top App Bar Actions ---
    object Notifications : Screen("notifications", "Notifications", Icons.Filled.Notifications)
    object Location : Screen("location", "Location", Icons.Filled.LocationOn)

    // --- Nested Booking Flow ---
    object Booking : Screen("booking/{scheduleId}", "Booking", null) {
        fun createRoute(scheduleId: String) = "booking/$scheduleId"
    }
    // ── Route-specific bus list ───────────────────────────────────────────────
    // Spaces in city names replaced with underscores for a safe URL path.
    object RouteSchedules : Screen(
        "route_schedules/{origin}/{destination}",
        "Route Schedules",
        null
    ) {
        fun createRoute(origin: String, destination: String): String {
            val safeOrigin = origin.replace(" ", "_")
            val safeDest   = destination.replace(" ", "_")
            return "route_schedules/$safeOrigin/$safeDest"
        }
    }


    // --- Navigation Drawer Screens ---

    object Routes: Screen("routes","Routes",Icons.AutoMirrored.Default.AltRoute)
    object Discover : Screen("discover", "Discover", Icons.Default.Explore)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
     object Help     : Screen("help",     "Help",     Icons.AutoMirrored.Filled.Help)
    object About    : Screen("about",    "About",    Icons.Default.Info)

    object ContactUs : Screen("contact_us", "Contact Us", Icons.Filled.Phone)
    object RateUs : Screen("rate_us", "Rate Us", Icons.Filled.Star)
    object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy", Icons.Filled.PrivacyTip)

    //
    object Companies : Screen("companies", "Bus Companies", Icons.Default.Business)
    object CompanyDetails : Screen("company/{companyId}", "Company Details", null) {
        fun createRoute(id: String) = "company/$id"
    }
    object Stations : Screen("stations", "Bus Stations", Icons.Default.LocationOn)
    object ParcelTracker : Screen("parcel_tracker", "Track Parcel", Icons.Default.Inventory)
    }





// Defines items for the Bottom Navigation Bar
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.MyTickets,
    Screen.Routes
)

// Defines items for the Navigation Drawer

