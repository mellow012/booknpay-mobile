package com.example.booknpay.ui.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.ui.home.UpcomingBooking

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeHeroSection(
    featuredTrip: UpcomingBooking?,
    boardingSoon: List<ScheduleDetails>,
    onBookingClick: () -> Unit,
    onScheduleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine how many pages: 1 (if trip exists) OR count of boarding buses (max 5)
    val pageCount = when {
        featuredTrip != null -> 1
        boardingSoon.isNotEmpty() -> boardingSoon.size.coerceAtMost(5)
        else -> 1 // For a default promo card
    }

    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp
        ) { page ->
            when {
                featuredTrip != null -> {
                    FeaturedTripCard(featuredTrip = featuredTrip, onViewBookings = onBookingClick)
                }
                boardingSoon.isNotEmpty() -> {
                    BoardingSoonCard(
                        schedule = boardingSoon[page],
                        onClick = { onScheduleClick(boardingSoon[page].schedule.id) }
                    )
                }
                else -> {
                    DefaultTravelPromoCard()
                }
            }
        }

        // Indicators for the Carousel
        if (featuredTrip == null && pageCount > 1) {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration)
                        MaterialTheme.colorScheme.primary else Color.LightGray.copy(0.5f)
                    Box(Modifier.padding(3.dp).clip(CircleShape).background(color).size(6.dp))
                }
            }
        }
    }
}

@Composable
fun FeaturedTripCard(featuredTrip: UpcomingBooking, onViewBookings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onViewBookings() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("YOUR NEXT TRIP", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(0.8f), fontWeight = FontWeight.Bold)
                Text("#${featuredTrip.bookingReference}", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.6f))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(featuredTrip.origin, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("Departure", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                }
                Icon(Icons.Default.TrendingFlat, null, tint = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(featuredTrip.destination, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("Destination", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("${featuredTrip.departureDate} • ${featuredTrip.departureTime}", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Seat: ${featuredTrip.seatNumbers}", color = Color.White)
            }
        }
    }
}

@Composable
fun BoardingSoonCard(schedule: ScheduleDetails, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)) // Sleek Dark Look
    ) {
        Box(Modifier.fillMaxSize()) {
            // Decorative background circle
            Box(Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 30.dp, y = (-30).dp).background(Color.White.copy(0.05f), CircleShape))

            Column(Modifier.padding(20.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                    Text("BOARDING SOON", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFFD32F2F), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                Column {
                    Text("${schedule.origin} → ${schedule.destination}", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Buses are departing shortly. Grab your seat!", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("MWK ${"%,d".format(schedule.schedule.price.toInt())}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun DefaultTravelPromoCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.DirectionsBus, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Explore Malawi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Search for your next adventure across the warm heart of Africa.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}