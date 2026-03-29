package com.example.booknpay.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknpay.ui.home.ActiveRoute
import com.example.booknpay.ui.theme.BrandIndigo

@Composable
fun PopularRoutesSection(
    routes: List<ActiveRoute>,
    onRouteClick: (origin: String, destination: String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (routes.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Popular routes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            TextButton(onClick = onViewAll) {
                Text("See all", fontWeight = FontWeight.SemiBold, color = BrandIndigo)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- The Horizontal List ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between cards
        ) {
            items(routes.take(6)) { route ->
                PopularRoutesCard(
                    route = route,
                    onClick = { onRouteClick(route.origin, route.destination) }
                )
            }
        }
    }
}

@Composable
 private fun PopularRoutesCard(
    route: ActiveRoute,
    onClick: () -> Unit,
) {
    // We'll use a Box to allow for a background "glow" effect
    Box(
        modifier = Modifier
            .width(260.dp) // Slightly wider for a premium feel
            .padding(vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Top Row: Emoji & Status
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(route.emoji, style = MaterialTheme.typography.displaySmall)

                    // "Live" indicator for active schedules
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF1B8A4E).copy(alpha = 0.1f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(6.dp).background(Color(0xFF1B8A4E), CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1B8A4E)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // The "Route Journey" Visual
                Column {
                    RoutePoint(city = route.origin, isStart = true)
                    // Custom dashed connector
                    Box(
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .height(20.dp)
                            .width(1.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                    RoutePoint(city = route.destination, isStart = false)
                }

                Spacer(Modifier.height(20.dp))

                // Pricing and CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Starting from",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "MWK ${"%,d".format(route.minPrice.toInt())}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = BrandIndigo // Use your Brand color here
                        )
                    }

                    // Iconic "Go" button
                    Surface(
                        shape = CircleShape,
                        color = BrandIndigo,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            null,
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutePoint(city: String, isStart: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .border(
                    width = 2.dp,
                    color = if (isStart) BrandIndigo else Color(0xFFE65100),
                    shape = CircleShape
                )
                .padding(3.dp)
        ) {
            if (isStart) {
                Box(Modifier.fillMaxSize().background(BrandIndigo, CircleShape))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = city,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
