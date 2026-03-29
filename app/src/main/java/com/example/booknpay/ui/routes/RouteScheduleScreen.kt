package com.example.booknpay.ui.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booknpay.data.model.ScheduleDetails
import com.example.booknpay.ui.route.RouteSchedulesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSchedulesScreen(
    onNavigateBack:  () -> Unit,
    onBookSchedule:  (String) -> Unit,
    viewModel: RouteSchedulesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        if (state.origin.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(state.origin, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.padding(horizontal = 6.dp).size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(state.destination, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Route schedules")
                        }
                        if (!state.isLoading) {
                            Text(
                                "${state.schedules.size} bus${if (state.schedules.size != 1) "es" else ""} available",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(state.error ?: "Failed to load schedules")
                    TextButton(onClick = onNavigateBack) { Text("Go back") }
                }
            }

            state.schedules.isEmpty() -> Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚌", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No buses on this route right now",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Check back soon or try another route",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.schedules, key = { it.schedule.id }) { sd ->
                    RouteScheduleCard(
                        details  = sd,
                        onBook   = { onBookSchedule(sd.schedule.id) }
                    )
                }
            }
        }
    }
}

// ── Individual schedule card on this screen ───────────────────────────────────
@Composable
private fun RouteScheduleCard(
    details: ScheduleDetails,
    onBook:  () -> Unit
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEE, MMM dd", Locale.getDefault()) }
    val depDate = details.schedule.departureDateTime?.toDate()
    val arrDate = details.schedule.arrivalDateTime?.toDate()
    val depTime = depDate?.let { timeFmt.format(it) } ?: "—"
    val arrTime = arrDate?.let { timeFmt.format(it) } ?: "—"
    val depDay  = depDate?.let { dateFmt.format(it) } ?: "—"

    val seatsLeft  = details.schedule.availableSeats
    val seatsColor = when {
        seatsLeft <= 5  -> MaterialTheme.colorScheme.error
        seatsLeft <= 15 -> MaterialTheme.colorScheme.tertiary
        else            -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Bus + company ─────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp).size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            details.schedule.busId,
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            details.schedule.companyId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "MWK ${details.schedule.price.toInt()}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // ── Timing row ────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("Departs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(depTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(depDay, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Arrives", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(arrTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Seats + Book ──────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.EventSeat, null, tint = seatsColor, modifier = Modifier.size(16.dp))
                    Text(
                        "$seatsLeft seats available",
                        style      = MaterialTheme.typography.bodySmall,
                        color      = seatsColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick  = onBook,
                    shape    = RoundedCornerShape(10.dp),
                    enabled  = seatsLeft > 0,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(if (seatsLeft > 0) "Book now" else "Fully booked", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}