package com.example.booknpay.ui.home


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.booknpay.ui.components.HomeScreenSkeleton
import com.example.booknpay.ui.components.SectionHeader
import com.example.booknpay.ui.home.components.*

@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToSearch: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onScheduleClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading && state.availableSchedules.isEmpty()) {
        HomeScreenSkeleton()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Custom Welcome Header (since you don't have HomeHeader)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${state.userName} 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Where are you travelling today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Notification Icon with Badge
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (state.notificationCount > 0) {
                                    Badge { Text(state.notificationCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            }

            // 2. Search Section
            item {
                SearchCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onNavigateToSearch = onNavigateToSearch
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Quick Actions
            item {
                QuickActions(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onNavigateToBookings = onNavigateToBookings,
                    onNavigateToProfile = onNavigateToProfile,
                    notificationCount = state.notificationCount,
                    onNavigateToNotifications = onNavigateToNotifications
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. Upcoming Trips (Using your SectionHeader)
            if (state.upcomingBookings.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Your Next Trips",
                        actionText = "See All",
                        onActionClick = onNavigateToBookings,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    UpcomingTripsRow(
                        bookings = state.upcomingBookings,
                        onBookingClick = { /* Navigate to Ticket */ }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 5. Available Schedules (Using your SectionHeader)
            item {
                SectionHeader(
                    title = "Available Schedules",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.availableSchedules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming buses found for today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.availableSchedules) { details ->
                    ScheduleItem(
                        schedule = details,
                        onScheduleClick = { onScheduleClick(details.schedule.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // 6. Popular Routes
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Popular Routes",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                PopularRoutesSection(
                    routes = state.popularRoutes,
                    onRouteClick = { _, _ -> /* Search filter */ }
                )
            }
        }
    }
}