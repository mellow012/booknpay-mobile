package com.example.booknpay.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IntegratedSearchCard(
    modifier: Modifier = Modifier,
    origin: String,
    destination: String,
    date: String,
    passengers: Int,
    onSearchClick: () -> Unit,
    onFieldClick: (SearchFieldType) -> Unit,
    onSwapClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Where to next?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                ) {
                    // Departure (Origin)
                    SearchInputRow(
                        icon = Icons.Default.TripOrigin,
                        label = "From",
                        value = origin.ifEmpty { "Select Departure" },
                        isPlaceholder = origin.isEmpty(),
                        onClick = { onFieldClick(SearchFieldType.ORIGIN) } // Check this Enum
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )

                    // Destination
                    SearchInputRow(
                        icon = Icons.Default.LocationOn,
                        label = "To",
                        value = destination.ifEmpty { "Select Destination" },
                        isPlaceholder = destination.isEmpty(),
                        onClick = { onFieldClick(SearchFieldType.DESTINATION) } // Check this Enum
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchInputRow(
                            modifier = Modifier.weight(1.2f),
                            icon = Icons.Default.CalendarToday,
                            label = "Date",
                            value = date,
                            onClick = { onFieldClick(SearchFieldType.DATE) }
                        )

                        Box(modifier = Modifier.width(0.5.dp).height(32.dp).background(Color.LightGray))

                        SearchInputRow(
                            modifier = Modifier.weight(0.8f),
                            icon = Icons.Default.Groups,
                            label = "Seats",
                            value = "$passengers Pass.",
                            onClick = { onFieldClick(SearchFieldType.PASSENGERS) }
                        )
                    }
                }

                // Swap Button
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, bottom = 48.dp)
                        .size(36.dp)
                        .shadow(4.dp, CircleShape)
                        .clickable { onSwapClick() },
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = "Swap",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(12.dp))
                Text("Search Schedules", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SearchInputRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    isPlaceholder: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isPlaceholder) Color.Gray.copy(alpha = 0.6f) else Color.Black
            )
        }
    }
}
enum class SearchFieldType { ORIGIN, DESTINATION, DATE, PASSENGERS,CURRENT_LOCATION }