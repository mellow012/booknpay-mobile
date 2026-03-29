package com.example.booknpay.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.data.repository.FirebaseRouteRepository
import com.example.booknpay.data.repository.FirebaseScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ─── ViewModel ────────────────────────────────────────────────────────────────
data class DiscoverState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val routes: List<Route> = emptyList(),
    val upcomingSchedules: List<ScheduleDetails> = emptyList(),
    val filteredRoutes: List<Route> = emptyList(),
    val popularOrigins: List<String> = emptyList(),
    val selectedOriginFilter: String? = null,
    val error: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val routeRepository: FirebaseRouteRepository,
    private val scheduleRepository: FirebaseScheduleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoverState())
    val state: StateFlow<DiscoverState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val routes = routeRepository.getRoutes()
                val origins = routes.map { it.origin }.distinct().sorted()

                combine(
                    scheduleRepository.getSchedulesRealtime(),
                    flow { emit(routes) }
                ) { schedules, routeList ->
                    val routeMap = routeList.associateBy { it.id }
                    val now = Date()
                    schedules
                        .filter { it.departureDateTime?.toDate()?.after(now) == true }
                        .take(8)
                        .mapNotNull { sch ->
                            routeMap[sch.routeId]?.let { r ->
                                ScheduleDetails(sch, r.origin, r.destination)
                            }
                        }
                        .sortedBy { it.schedule.departureDateTime?.seconds }
                }.collect { scheduleDetails ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            routes = routes,
                            filteredRoutes = routes,
                            upcomingSchedules = scheduleDetails,
                            popularOrigins = origins
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchChange(query: String) {
        _state.update { s ->
            val filtered = if (query.isBlank()) s.routes
            else s.routes.filter {
                it.origin.contains(query, ignoreCase = true) ||
                        it.destination.contains(query, ignoreCase = true)
            }
            s.copy(searchQuery = query, filteredRoutes = filtered)
        }
    }

    fun onOriginFilter(origin: String?) {
        _state.update { s ->
            val filtered = if (origin == null) s.routes
            else s.routes.filter { it.origin.equals(origin, ignoreCase = true) }
            s.copy(selectedOriginFilter = origin, filteredRoutes = filtered)
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    onRouteClick: (String, String) -> Unit,
    onScheduleClick: (String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Kinetic top bar — brand name + search icon
            TopAppBar(
                title = {
                    if (!searchActive) {
                        Text(
                            "Discover",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                actions = {
                    if (!searchActive) {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, "Search routes")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Search bar ─────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                RouteSearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::onSearchChange,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Origin filter chips ────────────────────────────────────────
            if (state.popularOrigins.isNotEmpty()) {
                item {
                    OriginFilterRow(
                        origins = state.popularOrigins,
                        selected = state.selectedOriginFilter,
                        onSelect = viewModel::onOriginFilter
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // ── Loading ────────────────────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                return@LazyColumn
            }

            // ── Upcoming schedules strip ───────────────────────────────────
            if (state.upcomingSchedules.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Departing soon",
                        actionLabel = null,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(state.upcomingSchedules) { sd ->
                            UpcomingScheduleChip(sd = sd, onClick = { onScheduleClick(sd.schedule.id) })
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            // ── Routes list ────────────────────────────────────────────────
            item {
                SectionHeader(
                    title = if (state.searchQuery.isBlank() && state.selectedOriginFilter == null)
                        "All routes (${state.filteredRoutes.size})"
                    else
                        "Results (${state.filteredRoutes.size})",
                    actionLabel = null,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (state.filteredRoutes.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", style = MaterialTheme.typography.displaySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No routes found", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Try a different search",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(state.filteredRoutes, key = { it.id }) { route ->
                    RouteCard(
                        route = route,
                        onClick = { onRouteClick(route.origin, route.destination) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────
@Composable
private fun RouteSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search city, route…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Clear, "Clear", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ─── Origin filter chips ──────────────────────────────────────────────────────
@Composable
private fun OriginFilterRow(
    origins: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("All") }
            )
        }
        items(origins) { origin ->
            FilterChip(
                selected = selected == origin,
                onClick = { onSelect(if (selected == origin) null else origin) },
                label = { Text(origin) }
            )
        }
    }
}

// ─── Upcoming schedule chip (horizontal scroll) ───────────────────────────────
@Composable
private fun UpcomingScheduleChip(
    sd: ScheduleDetails,
    onClick: () -> Unit
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val depTime = remember(sd.schedule.departureDateTime) {
        sd.schedule.departureDateTime?.toDate()?.let { timeFmt.format(it) } ?: "—"
    }
    val seatsLeft = sd.schedule.availableSeats
    val seatsColor = when {
        seatsLeft <= 5 -> MaterialTheme.colorScheme.error
        seatsLeft <= 15 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    depTime,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = seatsColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        "$seatsLeft seats",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = seatsColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    sd.origin,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    modifier = Modifier.padding(horizontal = 6.dp).size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    sd.destination,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "MWK ${sd.schedule.price.toInt()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ─── Route card ───────────────────────────────────────────────────────────────
@Composable
private fun RouteCard(
    route: Route,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Route icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Route,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        route.origin,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier.padding(horizontal = 6.dp).size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        route.destination,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${route.distance} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${route.duration} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Section header ───────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String?,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}