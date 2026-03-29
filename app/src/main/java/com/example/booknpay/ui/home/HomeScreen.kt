package com.example.booknpay.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.ui.components.HomeScreenSkeleton
import com.example.booknpay.ui.components.rememberBase64Image
import com.example.booknpay.ui.home.components.*
import com.example.booknpay.ui.notifications.NotificationViewModel
import com.example.booknpay.util.getTimeBasedGreeting
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// --- Color Palette ---
private val BrandIndigo = Color(0xFF2B3896)
private val BrandBlue = Color(0xFF0073B7)
private val RouteOrange = Color(0xFFE65100)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToParcels: () -> Unit,
    onMenuClick: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onScheduleClick: (String) -> Unit,
    onRouteClick: (String, String) -> Unit,
    onSearchWithQuery: (String, String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    var currentSheet by remember { mutableStateOf<SearchFieldType?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dynamic Greeting State
    var greeting by remember { mutableStateOf(getTimeBasedGreeting()) }

    LaunchedEffect(Unit) {
        while (true) {
            greeting = getTimeBasedGreeting()
            kotlinx.coroutines.delay(60000) // Refresh every minute
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.onLocationTriggerClicked()
        }
    }

    // --- Bottom Sheets ---
    if (currentSheet != null) {
        ModalBottomSheet(onDismissRequest = { currentSheet = null }) {
            when (currentSheet) {
                SearchFieldType.CURRENT_LOCATION -> LocationPickerSheet(
                    title = "Set Your City",
                    availableCities = state.availableOrigins,
                    selectedCity = state.currentLocation,
                    onSelect = {
                        viewModel.updateCurrentLocation(it)
                        currentSheet = null
                    }
                )
                SearchFieldType.ORIGIN -> LocationPickerSheet(
                    title = "Select Departure",
                    availableCities = state.availableOrigins,
                    selectedCity = state.selectedOrigin,
                    onSelect = { viewModel.onOriginSelected(it); currentSheet = null }
                )
                SearchFieldType.DESTINATION -> LocationPickerSheet(
                    title = "Select Destination",
                    availableCities = state.availableOrigins,
                    selectedCity = state.selectedDestination,
                    onSelect = { viewModel.onDestinationSelected(it); currentSheet = null }
                )
                SearchFieldType.PASSENGERS -> PassengerPickerSheet(
                    currentCount = state.passengerCount,
                    onSelect = { viewModel.onPassengerCountChanged(it); currentSheet = null }
                )
                else -> {}
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initialDateMillis = state.selectedDateMillis,
            onDateSelected = { viewModel.onDateSelected(it); showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    if (state.isLoading && state.popularBuses.isEmpty()) {
        HomeScreenSkeleton()
    } else {
        Scaffold(
            topBar = {
                HomeTopBar(
                    userInitials = state.userInitials,
                    currentLocation = state.currentLocation,
                    isLocating = state.isLocating,
                    unreadCount = unreadCount,
                    onProfileClick = onNavigateToProfile,
                    onLocationClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) viewModel.onLocationTriggerClicked()
                        else permissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    },
                    onManualLocationClick = { currentSheet = SearchFieldType.CURRENT_LOCATION },
                    onNotificationsClick = onNavigateToNotifications
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Dynamic Greeting Section
                item {
                    Column(Modifier.padding(16.dp, vertical = 16.dp)) {
                        Text(
                            text = greeting.first, // Chichewa
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandIndigo,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.userName.ifEmpty { "Traveler" }}! \uD83D\uDC4B",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${greeting.second}. Where are we traveling to today?", // English
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. Hero Section
                item {
                    HomeHeroSection(
                        featuredTrip = state.featuredTrip,
                        boardingSoon = state.boardingSoon,
                        onBookingClick = onNavigateToBookings,
                        onScheduleClick = onScheduleClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // 3. Search Section
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        IntegratedSearchCard(
                            origin = state.selectedOrigin ?: "",
                            destination = state.selectedDestination ?: "",
                            date = state.formattedDate,
                            passengers = state.passengerCount,
                            onSearchClick = {
                                if (!state.selectedOrigin.isNullOrBlank() && !state.selectedDestination.isNullOrBlank()) {
                                    onSearchWithQuery(state.selectedOrigin!!, state.selectedDestination!!)
                                } else {
                                    viewModel.searchBuses()
                                }
                            },
                            onFieldClick = { field ->
                                if (field == SearchFieldType.DATE) showDatePicker = true
                                else currentSheet = field
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // 4. Quick Actions
                item {
                    QuickActionGrid(
                        actions = state.quickActions,
                        onActionClick = { actionType ->
                            when(actionType) {
                                ActionType.BOOK_BUS -> onNavigateToSearch()
                                ActionType.MY_TICKETS -> onNavigateToBookings()
                                ActionType.SCHEDULES -> onNavigateToRoutes()
                                ActionType.PARCELS -> onNavigateToParcels()
                                ActionType.WALLET -> {
                                    scope.launch { snackbarHostState.showSnackbar("Wallet integration coming soon!") }
                                }
                                else -> {}
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                }

                // 5. Popular Buses
                item {
                    PopularBusesSection(
                        buses = state.popularBuses,
                        onBookBus = onScheduleClick,
                        onViewAll = {}
                    )
                }

                // 6. Styled Popular Routes
                item {
                    PopularRoutesSection(
                        routes = state.activeRoutes,
                        onRouteClick = onRouteClick,
                        onViewAll = { onNavigateToRoutes() }
                    )
                }

                // 7. Search Results
                if (state.isSearching) {
                    item { Box(Modifier.fillMaxWidth(), Alignment.Center) { CircularProgressIndicator() } }
                } else if (state.searchResults.isNotEmpty()) {
                    items(state.searchResults, key = { it.schedule.id }) { sd ->
                        BusResultCard(
                            details = sd,
                            onBook = { onScheduleClick(sd.schedule.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    userInitials: String,
    currentLocation: String?,
    isLocating: Boolean,
    unreadCount: Int,
    onProfileClick: () -> Unit,
    onLocationClick: () -> Unit,
    onManualLocationClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Surface(
                onClick = onManualLocationClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.clickable { onLocationClick() }) {
                        if (isLocating) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.MyLocation, null, Modifier.size(18.dp), tint = BrandBlue)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(currentLocation ?: "Select Location", style = MaterialTheme.typography.labelLarge)
                    Icon(Icons.Default.ArrowDropDown, null, Modifier.size(20.dp))
                }
            }
        },
        navigationIcon = {
            Surface(
                modifier = Modifier.padding(start = 12.dp).size(38.dp).clickable { onProfileClick() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(userInitials, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        actions = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(Icons.Default.Notifications, "Notifications")
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .background(Color.Red, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

// Re-using your PopularRoute components with the styling we defined earlier
@Composable
fun PopularRoutesSection(
    routes: List<com.example.booknpay.ui.home.ActiveRoute>,
    onRouteClick: (String, String) -> Unit,
    onViewAll: () -> Unit
) {
    if (routes.isEmpty()) return
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Popular routes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            TextButton(onClick = onViewAll) { Text("See all", color = BrandIndigo) }
        }
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(routes.take(6)) { route ->
                PopularRouteCard(route, onClick = { onRouteClick(route.origin, route.destination) })
            }
        }
    }
}

@Composable
private fun PopularRouteCard(route: com.example.booknpay.ui.home.ActiveRoute, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(260.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(route.emoji, style = MaterialTheme.typography.headlineMedium)
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1B8A4E).copy(0.1f)) {
                    Text("ACTIVE", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF1B8A4E))
                }
            }
            Spacer(Modifier.height(16.dp))
            RoutePoint(route.origin, true)
            Box(Modifier.padding(start = 6.dp).height(16.dp).width(1.dp).background(Color.LightGray))
            RoutePoint(route.destination, false)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                Column {
                    Text("From MWK", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("%,d".format(route.minPrice.toInt()), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = BrandIndigo)
                }
                Surface(shape = CircleShape, color = BrandIndigo, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RoutePoint(city: String, isStart: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).border(2.dp, if (isStart) BrandIndigo else RouteOrange, CircleShape).padding(2.dp)) {
            if (isStart) Box(Modifier.fillMaxSize().background(BrandIndigo, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Text(city, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
// ─── Bus Result Card ──────────────────────────────────────────────────────────

// ─── Quick Actions Grid ───────────────────────────────────────────────────────



@Composable

fun QuickActionGrid(

    actions: List<QuickActionModel>,

    onActionClick: (ActionType) -> Unit,

    modifier: Modifier = Modifier

) {

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {

        actions.chunked(2).forEach { rowItems ->

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                rowItems.forEach { action ->

                    Card(

                        modifier = Modifier.weight(1f).height(90.dp),

                        onClick = { onActionClick(action.actionType) },

                        shape = RoundedCornerShape(16.dp),

                        colors = CardDefaults.cardColors(

                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

                        )

                    ) {

                        Column(

                            Modifier.fillMaxSize(),

                            verticalArrangement = Arrangement.Center,

                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {

                            Icon(imageVector = action.icon, contentDescription = null, tint = BrandBlue)

                            Spacer(Modifier.height(8.dp))

                            Text(action.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                        }

                    }

                }

            }

        }

    }

}

@Composable

fun BusResultCard(

    modifier: Modifier = Modifier,

    details: ScheduleDetails,

    onBook: () -> Unit

) {

    val company = details.company

    val logoBitmap = rememberBase64Image(company?.logo)



    Card(

        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(containerColor = Color.White),

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)

    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                if (logoBitmap != null) {

                    Image(

                        bitmap = logoBitmap,

                        contentDescription = company?.name,

                        modifier = Modifier

                            .size(48.dp)

                            .clip(RoundedCornerShape(8.dp)),

                        contentScale = ContentScale.Crop

                    )

                } else {

                    Box(

                        modifier = Modifier

                            .size(48.dp)

                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)),

                        contentAlignment = Alignment.Center

                    ) {

                        Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color.Gray)

                    }

                }



                Spacer(modifier = Modifier.width(12.dp))



                Column {

                    Text(

                        text = company?.name ?: "Company Name",

                        style = MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.Bold

                    )

                    Text(

                        text = company?.description ?: "Reliable transport services",

                        style = MaterialTheme.typography.bodySmall,

                        color = Color.Gray,

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis

                    )

                }

            }



            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)



            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Column {

                    Text(text = details.origin, style = MaterialTheme.typography.labelMedium)

                    Text(

                        text = details.schedule.departureDateTime?.toDate()?.let {

                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)

                        } ?: "--:--",

                        style = MaterialTheme.typography.headlineSmall,

                        fontWeight = FontWeight.Bold

                    )

                }



                Icon(

                    imageVector = Icons.Default.DirectionsBus,

                    contentDescription = null,

                    tint = BrandBlue,

                    modifier = Modifier.size(24.dp)

                )



                Column(horizontalAlignment = Alignment.End) {

                    Text(text = details.destination, style = MaterialTheme.typography.labelMedium)

                    Text(

                        text = details.schedule.arrivalDateTime?.toDate()?.let {

                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)

                        } ?: "--:--",

                        style = MaterialTheme.typography.headlineSmall,

                        fontWeight = FontWeight.Bold

                    )

                }

            }



            Spacer(modifier = Modifier.height(16.dp))



            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Column {

                    Text(text = "Price", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                    Text(

                        text = "MWK ${"%,.0f".format(details.schedule.price)}",

                        style = MaterialTheme.typography.titleLarge,

                        color = Color(0xFF2E7D32),

                        fontWeight = FontWeight.ExtraBold

                    )

                }



                Button(

                    onClick = onBook,

                    shape = RoundedCornerShape(8.dp),

                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)

                ) {

                    Text("Book Seat")

                }

            }

        }

    }

}



// ─── Bottom sheet composables ─────────────────────────────────────────────────



@Composable

fun LocationPickerSheet(title: String, availableCities: List<String>, selectedCity: String?, onSelect: (String) -> Unit) {

    Column(Modifier.fillMaxWidth().padding(24.dp)) {

        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))



        if (availableCities.isEmpty()) {

            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {

                CircularProgressIndicator()

            }

        } else {

            LazyColumn {

                items(availableCities) { city ->

                    val selected = city == selectedCity

                    Surface(

                        modifier = Modifier.fillMaxWidth().clickable { onSelect(city) },

                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,

                        shape = RoundedCornerShape(12.dp)

                    ) {

                        Row(

                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Icon(

                                imageVector = Icons.Default.LocationOn,

                                contentDescription = null,

                                modifier = Modifier.size(18.dp),

                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                            )

                            Spacer(Modifier.width(10.dp))

                            Text(

                                text = city,

                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,

                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,

                                modifier = Modifier.weight(1f)

                            )

                            if (selected) {

                                Icon(

                                    imageVector = Icons.Default.Check,

                                    contentDescription = null,

                                    modifier = Modifier.size(18.dp),

                                    tint = MaterialTheme.colorScheme.primary

                                )

                            }

                        }

                    }

                    Spacer(Modifier.height(2.dp))

                }

            }

        }

        Spacer(Modifier.height(24.dp))

    }

}



@Composable

fun PassengerPickerSheet(currentCount: Int, onSelect: (Int) -> Unit) {

    Column(Modifier.padding(24.dp)) {

        Text("Number of Passengers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            (1..5).forEach { n ->

                FilterChip(

                    selected = n == currentCount,

                    onClick = { onSelect(n) },

                    label = { Text("$n") },

                    modifier = Modifier.weight(1f)

                )

            }

        }

        Spacer(Modifier.height(24.dp))

    }

}
// Note: BusResultCard, LocationPickerSheet, and PassengerPickerSheet logic remains as you provided.