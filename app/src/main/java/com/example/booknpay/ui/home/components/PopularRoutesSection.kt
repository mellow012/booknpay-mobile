package com.example.booknpay.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknpay.ui.home.PopularRoute

@Composable
fun PopularRoutesSection(
    routes: List<PopularRoute>,
    onRouteClick: (String, String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Popular Routes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(routes) { route ->
                PopularRouteCard(route = route, onRouteClick = onRouteClick)
            }
        }
    }
}

@Composable
private fun PopularRouteCard(
    route: PopularRoute,
    onRouteClick: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onRouteClick(route.origin, route.destination) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = route.emoji,
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${route.origin}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "to ${route.destination}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${route.tripCount} trips",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PopularRoutesSectionPreview() {
    PopularRoutesSection(
        routes = listOf(
            PopularRoute(
                emoji = "✈️",
                origin = "Nairobi",
                destination = "Mombasa",
                tripCount = 5
            ),
            PopularRoute(
                emoji = "🚌",
                origin = "Kisumu",
                destination = "Nakuru",
                tripCount = 3),
            PopularRoute(
                emoji = "🚗",
                origin = "Mombasa",
                destination = "Nairobi",
                tripCount = 6)
        ),
        onRouteClick = { _, _ -> }
    )
}
