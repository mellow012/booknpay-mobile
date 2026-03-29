package com.example.booknpay.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booknpay.data.model.ScheduleDetails
import java.text.SimpleDateFormat
import java.util.*

private val BrandIndigo = Color(0xFF2B3896)
private val BrandBlue   = Color(0xFF0073B7)
private val OrangeWarm  = Color(0xFFFF8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onScheduleClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = { Text("Discover", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )

                // Global Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search companies, cities, or routes...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = BrandBlue) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = BrandBlue
                    )
                )

                // Quick Discovery Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Companies", "Buses")
                    // Use 'items' for LazyRow content, not 'forEach'
                    items(filters) { filter ->
                        FilterChip(
                            selected = state.activeFilter == filter,
                            onClick = { viewModel.onFilterChange(filter) },
                            label = { Text(if (filter == "Companies") "Operators" else filter) },
                            leadingIcon = {
                                val icon = when (filter) {
                                    "Companies" -> Icons.Default.Business
                                    "Buses" -> Icons.Default.DirectionsBus
                                    else -> null
                                }
                                icon?.let { Icon(it, null, Modifier.size(16.dp)) }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.searchQuery.isEmpty() && state.searchResults.isEmpty()) {
                    item {
                        Text("Top Bus Operators", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    // Place for operator grid
                } else if (state.searchResults.isEmpty()) {
                    item {
                        EmptyState(
                            emoji = "🔍",
                            title = "No results found",
                            subtitle = "Try searching for a city like 'Blantyre'"
                        )
                    }
                } else {
                    items(state.searchResults, key = { it.schedule.id }) { result ->
                        GlobalResultCard(result, onClick = { onScheduleClick(result.schedule.id) })
                    }
                }
            }
        }

        // Error Dialog Fix
        state.error?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Error") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
private fun GlobalResultCard(result: ScheduleDetails, onClick: () -> Unit) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val departure = result.schedule.departureDateTime?.toDate()?.let { timeFmt.format(it) } ?: "--:--"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = BrandIndigo) {
                Box(contentAlignment = Alignment.Center) {
                    Text((result.company?.name ?: "B").take(1), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${result.origin} → ${result.destination}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(result.company?.name ?: "Operator", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("MWK ${"%,.0f".format(result.schedule.price)}", fontWeight = FontWeight.Black, color = BrandBlue)
                Text("Departs $departure", style = MaterialTheme.typography.labelSmall, color = OrangeWarm)
            }
        }
    }
}

@Composable
private fun EmptyState(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}