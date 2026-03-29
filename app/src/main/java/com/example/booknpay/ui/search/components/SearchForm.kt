package com.example.booknpay.ui.search.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknpay.data.model.Route
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchForm(
    origin: String,
    destination: String,
    selectedDate: Date,
    passengerCount: Int,
    availableRoutes: List<Route>,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onDateChange: (Date) -> Unit,
    onPassengerCountChange: (Int) -> Unit,
    onSearchClick: () -> Unit,
    isLoading: Boolean
) {
    var showOriginDropdown      by remember { mutableStateOf(false) }
    var showDestinationDropdown by remember { mutableStateOf(false) }
    var showDatePicker          by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault()) }
    val origins      = remember(availableRoutes) { availableRoutes.map { it.origin }.distinct().sorted() }
    val destinations = remember(availableRoutes) { availableRoutes.map { it.destination }.distinct().sorted() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── From ──────────────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded         = showOriginDropdown,
                onExpandedChange = { showOriginDropdown = it }
            ) {
                OutlinedTextField(
                    value         = origin,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("From") },
                    leadingIcon   = {
                        Icon(
                            Icons.Default.TripOrigin,
                            contentDescription = null,
                            tint   = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOriginDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape  = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(expanded = showOriginDropdown, onDismissRequest = { showOriginDropdown = false }) {
                    origins.forEach { city ->
                        DropdownMenuItem(
                            text    = { Text(city) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp)) },
                            onClick = { onOriginChange(city); showOriginDropdown = false }
                        )
                    }
                }
            }

            // ── Swap hint ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Swap",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ── To ────────────────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded         = showDestinationDropdown,
                onExpandedChange = { showDestinationDropdown = it }
            ) {
                OutlinedTextField(
                    value         = destination,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("To") },
                    leadingIcon   = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint   = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDestinationDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape  = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(expanded = showDestinationDropdown, onDismissRequest = { showDestinationDropdown = false }) {
                    destinations.forEach { city ->
                        DropdownMenuItem(
                            text    = { Text(city) },
                            leadingIcon = { Icon(Icons.Default.Place, null, modifier = Modifier.size(16.dp)) },
                            onClick = { onDestinationChange(city); showDestinationDropdown = false }
                        )
                    }
                }
            }

            // ── Date + Passengers row ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date picker trigger
                OutlinedTextField(
                    value         = dateFormat.format(selectedDate),
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Date") },
                    leadingIcon   = {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true },
                    shape  = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                // Passenger count stepper
                PassengerStepper(
                    count     = passengerCount,
                    onMinus   = { onPassengerCountChange(passengerCount - 1) },
                    onPlus    = { onPassengerCountChange(passengerCount + 1) },
                    modifier  = Modifier.weight(1f)
                )
            }

            // ── Search button ─────────────────────────────────────────────
            Button(
                onClick  = onSearchClick,
                enabled  = !isLoading && origin.isNotEmpty() && destination.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color    = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "Search buses",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // ── Date picker dialog ────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateChange(Date(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

// ── Passenger count stepper ───────────────────────────────────────────────────
@Composable
private fun PassengerStepper(
    count:    Int,
    onMinus:  () -> Unit,
    onPlus:   () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text  = "Passengers",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick  = onMinus,
                    enabled  = count > 1,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease passengers",
                        tint = if (count > 1) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text       = "$count",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick  = onPlus,
                    enabled  = count < 20,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase passengers",
                        tint = if (count < 20) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}