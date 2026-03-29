package com.example.booknpay.ui.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Color tokens for Discover screen ─────────────────────────────────────────
private val DiscoverBlue   = Color(0xFF2D3AC1)
private val DiscoverIndigo = Color(0xFF3D4FD6)
private val SoftBg         = Color(0xFFF0F1FB)
private val GoldAccent     = Color(0xFFB87333)
private val StarGold       = Color(0xFFFFC107)

// ── Data models ───────────────────────────────────────────────────────────────
data class BusOperator(
    val id: String,
    val name: String,
    val subtitle: String,
    val rating: Double,
    val reviewCount: String,
    val description: String,
    val amenities: List<String>,
    val popularRoutes: List<String>,
    val emoji: String
)

data class Departure(
    val time: String,
    val price: String
)

data class TopOperator(
    val name: String,
    val tagline: String,
    val emoji: String
)

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onNavigateToSearch: () -> Unit = {},
    onOperatorClick: (String) -> Unit = {},
    onViewSchedule: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val featuredOperator = BusOperator(
        id          = "zingbus",
        name        = "Zingbus Premium",
        subtitle    = "FEATURED OPERATOR",
        rating      = 4.8,
        reviewCount = "12k+",
        description = "Zingbus offers a standardized intercity travel experience with premium lounges, GPS tracking, and climate-controlled cabins across 400+ routes.",
        amenities   = listOf("Free WiFi", "Charging Points", "Verified Staff"),
        popularRoutes = listOf("Delhi → Chandigarh", "Mumbai → Pune"),
        emoji       = "🚌"
    )

    val departures = listOf(
        Departure("22:30", "₹799"),
        Departure("23:15", "₹850"),
        Departure("00:05", "₹799")
    )

    val topOperators = listOf(
        TopOperator("IntrCity SmartBus",  "Lounge access • On-time guarantee", "🚌"),
        TopOperator("Greenline Travels",  "Electric fleet • AC Sleeper",       "🌿"),
        TopOperator("Orange Express",     "Budget friendly • Daily runs",      "🟠")
    )

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(SoftBg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── Top App Bar ────────────────────────────────────────────────────
        item {
            DiscoverTopBar(onNotificationClick = {})
        }

        // ── Hero Banner ────────────────────────────────────────────────────
        item {
            HeroBanner(
                searchQuery     = searchQuery,
                onSearchChange  = { searchQuery = it },
                onExploreClick  = onNavigateToSearch
            )
        }

        // ── Featured Operator ──────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(24.dp))
            FeaturedOperatorCard(
                operator    = featuredOperator,
                departures  = departures,
                onViewFull  = { onViewSchedule(featuredOperator.id) },
                onOperatorClick = { onOperatorClick(featuredOperator.id) }
            )
        }

        // ── Top Rated Header ───────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text       = "Top Rated Operators",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Top Operators List ─────────────────────────────────────────────
        items(topOperators) { operator ->
            TopOperatorRow(
                operator   = operator,
                onClick    = { onOperatorClick(operator.name) }
            )
        }
    }
}

// ── Top App Bar ───────────────────────────────────────────────────────────────
@Composable
private fun DiscoverTopBar(onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Menu,
            contentDescription = "Menu",
            tint               = Color(0xFF1A1A2E),
            modifier           = Modifier.size(26.dp)
        )

        Text(
            text       = "TibhukeBus",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = DiscoverBlue
        )

        // Avatar circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DiscoverBlue),
            contentAlignment = Alignment.Center
        ) {
            Text("T", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
}

// ── Hero Banner ───────────────────────────────────────────────────────────────
@Composable
private fun HeroBanner(
    searchQuery:    String,
    onSearchChange: (String) -> Unit,
    onExploreClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, SoftBg)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Text(
                text       = "Discover Premium\nBus Operators",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = DiscoverBlue,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Search row
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder   = { Text("Search here...", color = Color.Gray) },
                    leadingIcon   = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = DiscoverBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor   = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick  = onExploreClick,
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = DiscoverBlue),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Explore", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Featured Operator Card ─────────────────────────────────────────────────────
@Composable
private fun FeaturedOperatorCard(
    operator:       BusOperator,
    departures:     List<Departure>,
    onViewFull:     () -> Unit,
    onOperatorClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Label
        Text(
            text       = operator.subtitle,
            style      = MaterialTheme.typography.labelSmall,
            color      = GoldAccent,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Main card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Header: name + rating
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = operator.name,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    RatingBadge(rating = operator.rating, reviewCount = operator.reviewCount)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Operator logo placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = operator.emoji + " " + operator.name.split(" ").first(),
                        fontSize = 22.sp,
                        color    = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text      = operator.description,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Color(0xFF555555),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amenity chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(operator.amenities) { amenity ->
                        AmenityChip(label = amenity)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(12.dp))

                // Popular routes
                operator.popularRoutes.forEachIndexed { index, route ->
                    PopularRouteRow(
                        route     = route,
                        servicesText = if (index == 0) "15+ Daily Services" else "22+ Daily Services",
                        isPopular = index == 0,
                        onClick   = onOperatorClick
                    )
                    if (index < operator.popularRoutes.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Next Departures
                NextDeparturesCard(departures = departures, onViewFull = onViewFull)
            }
        }
    }
}

// ── Rating Badge ──────────────────────────────────────────────────────────────
@Composable
private fun RatingBadge(rating: Double, reviewCount: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DiscoverBlue
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint     = StarGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text       = "$rating ($reviewCount\nreviews)",
                style      = MaterialTheme.typography.labelSmall,
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                lineHeight = 14.sp
            )
        }
    }
}

// ── Amenity Chip ──────────────────────────────────────────────────────────────
@Composable
private fun AmenityChip(label: String) {
    val icon: ImageVector = when {
        label.contains("WiFi", ignoreCase = true)     -> Icons.Default.Wifi
        label.contains("Charging", ignoreCase = true) -> Icons.Default.BatteryChargingFull
        else                                           -> Icons.Default.CheckCircle
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SoftBg
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(13.dp), tint = DiscoverBlue)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF444444)
            )
        }
    }
}

// ── Popular Route Row ─────────────────────────────────────────────────────────
@Composable
private fun PopularRouteRow(
    route:        String,
    servicesText: String,
    isPopular:    Boolean,
    onClick:      () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.SwapVert,
                null,
                tint     = DiscoverBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text       = route,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = servicesText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        if (isPopular) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFFFEDD5)
            ) {
                Text(
                    text     = "POPULAR",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Color(0xFFD97706),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ── Next Departures Card ──────────────────────────────────────────────────────
@Composable
private fun NextDeparturesCard(
    departures: List<Departure>,
    onViewFull: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = SoftBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime,
                    null,
                    tint     = DiscoverBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text       = "Next Departures",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = DiscoverBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            departures.forEach { dep ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text  = "DEPARTURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text       = dep.time,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text  = "PRICE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text       = dep.price,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = DiscoverBlue
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFE8E8F0), thickness = 0.5.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick  = onViewFull,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = DiscoverBlue)
            ) {
                Text("View Full Schedule", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Top Operator Row ──────────────────────────────────────────────────────────
@Composable
private fun TopOperatorRow(
    operator: TopOperator,
    onClick:  () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SoftBg),
            contentAlignment = Alignment.Center
        ) {
            Text(operator.emoji, fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = operator.name,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = DiscoverBlue
            )
            Text(
                text  = operator.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint     = DiscoverBlue,
            modifier = Modifier.size(20.dp)
        )
    }
}
