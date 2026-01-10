package com.example.booknpay.ui.search


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booknpay.ui.components.*
import com.example.booknpay.ui.home.components.ScheduleItem
import com.example.booknpay.ui.search.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onScheduleClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    // Material 3 Pull to Refresh State
    val pullToRefreshState = rememberPullToRefreshState()

    // 1. Error Dialog Logic
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Default.Error, contentDescription = null) },
            title = { Text("Error") },
            text = { Text(state.error ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Schedules") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.hasSearched) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, "Filter")
                        }
                    }
                }
            )
        }
    ) { padding ->
        // 2. PullToRefreshBox replaces Box + PullRefreshIndicator
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Form Section
                SearchForm(
                    origin = state.origin,
                    destination = state.destination,
                    selectedDate = state.selectedDate,
                    availableRoutes = state.availableRoutes,
                    onOriginChange = viewModel::onOriginChange,
                    onDestinationChange = viewModel::onDestinationChange,
                    onDateChange = viewModel::onDateChange,
                    onSearchClick = viewModel::searchSchedules,
                    isLoading = state.isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Results Handling Logic
                when {
                    state.isLoading && !state.isRefreshing -> {
                        SearchResultsSkeleton()
                    }
                    !state.hasSearched -> {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = "Start Your Journey",
                            message = "Search for bus schedules to your destination"
                        )
                    }
                    state.searchResults.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = "No Schedules Found",
                            message = "Try searching for a different date or route"
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "${state.searchResults.size} schedules found",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            items(state.searchResults) { schedule ->
                                ScheduleItem(
                                    schedule = schedule,
                                    onScheduleClick = onScheduleClick
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Filter Bottom Sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                sortBy = state.sortBy,
                priceRange = state.priceRange,
                onSortChange = viewModel::onSortChange,
                onPriceRangeChange = viewModel::onPriceRangeChange,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}