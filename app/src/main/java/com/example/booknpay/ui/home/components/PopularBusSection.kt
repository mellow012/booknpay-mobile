package com.example.booknpay.ui.home.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.booknpay.ui.home.PopularBus
import java.util.Locale

@Composable
private fun CompanyAvatar(
    logoUrl: String?,
    companyName: String,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    SubcomposeAsyncImage(
        model = logoUrl,
        contentDescription = companyName,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        loading = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(size/2), strokeWidth = 2.dp)
            }
        },
        error = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (companyName.isNotEmpty()) companyName.take(1).uppercase() else "B",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PopularBusesSection(
    buses: List<PopularBus>,
    onBookBus: (String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredBuses = buses.take(3)
    val remainingBuses = buses.drop(3)

    if (featuredBuses.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { featuredBuses.size })

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open for Booking", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
            TextButton(onClick = onViewAll) {
                Text("See All", fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            BusCard(bus = featuredBuses[page], onBook = { onBookBus(featuredBuses[page].scheduleId) })
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(featuredBuses.size) { i ->
                val selected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .animateContentSize()
                        .size(if (selected) 24.dp else 8.dp, 8.dp)
                )
            }
        }

        if (remainingBuses.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                remainingBuses.forEach { bus ->
                    BusCardVertical(bus = bus, onBook = { onBookBus(bus.scheduleId) })
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BusCard(bus: PopularBus, onBook: () -> Unit) {
    // FIX: Handling nullability for UI safety
    val companyName = bus.companyName ?: "Unknown Bus"
    val origin = bus.origin ?: "TBD"
    val destination = bus.destination ?: "TBD"

    val isLowAvailability = bus.seatsLeft in 1..5
    val isFull = bus.seatsLeft == 0
    val seatsColor = when {
        isFull -> MaterialTheme.colorScheme.error
        isLowAvailability -> Color(0xFFFFA000)
        else -> Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(230.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f), MaterialTheme.colorScheme.surface))
        )) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompanyAvatar(logoUrl = bus.companyLogoUrl, companyName = companyName, size = 48.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(companyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Executive Class", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    // FIX: Added Locale.getDefault()
                    Text(
                        text = "MWK ${String.format(Locale.getDefault(), "%,d", bus.price.toInt())}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(bus.departureTime ?: "--:--", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = seatsColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventSeat, null, Modifier.size(14.dp), tint = seatsColor)
                            Spacer(Modifier.width(4.dp))
                            Text(text = if (!isFull) "${bus.seatsLeft} seats left" else "Full", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = seatsColor)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(origin, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(horizontal = 10.dp).size(20.dp))
                    Text(destination, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onBook,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFull,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isLowAvailability) Color(0xFFFFA000) else MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = if (!isFull) "Secure Your Seat" else "Bus Full", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BusCardVertical(bus: PopularBus, onBook: () -> Unit) {
    // FIX: Handling nullability for UI safety
    val companyName = bus.companyName ?: "Unknown Bus"
    val origin = bus.origin ?: "TBD"
    val destination = bus.destination ?: "TBD"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CompanyAvatar(logoUrl = bus.companyLogoUrl, companyName = companyName, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(companyName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("$origin → $destination", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Departs ${bus.departureTime ?: "--:--"}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                // FIX: Added Locale.getDefault()
                Text(
                    text = "MWK ${String.format(Locale.getDefault(), "%,d", bus.price.toInt())}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onBook, contentPadding = PaddingValues(0.dp)) {
                    Text("Book →", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}