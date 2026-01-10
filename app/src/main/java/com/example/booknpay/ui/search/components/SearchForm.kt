package com.example.booknpay.ui.search.components



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    availableRoutes: List<Route>,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onDateChange: (Date) -> Unit,
    onSearchClick: () -> Unit,
    isLoading: Boolean
) {
    var showOriginDropdown by remember { mutableStateOf(false) }
    var showDestinationDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
    val origins = remember(availableRoutes) {
        availableRoutes.map { it.origin }.distinct().sorted()
    }
    val destinations = remember(availableRoutes) {
        availableRoutes.map { it.destination }.distinct().sorted()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Origin
            ExposedDropdownMenuBox(
                expanded = showOriginDropdown,
                onExpandedChange = { showOriginDropdown = it }
            ) {
                OutlinedTextField(
                    value = origin,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("From") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOriginDropdown)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, "Origin")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        // FIX: Added AnchorType and enabled parameters
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded = showOriginDropdown,
                    onDismissRequest = { showOriginDropdown = false }
                ) {
                    origins.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                onOriginChange(city)
                                showOriginDropdown = false
                            }
                        )
                    }
                }
            }

            // Destination
            ExposedDropdownMenuBox(
                expanded = showDestinationDropdown,
                onExpandedChange = { showDestinationDropdown = it }
            ) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("To") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDestinationDropdown)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Place, "Destination")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        // FIX: Added AnchorType and enabled parameters
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded = showDestinationDropdown,
                    onDismissRequest = { showDestinationDropdown = false }
                ) {
                    destinations.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                onDestinationChange(city)
                                showDestinationDropdown = false
                            }
                        )
                    }
                }
            }

            // Date Picker (Unchanged, but ensure it's functional)
            OutlinedTextField(
                value = dateFormat.format(selectedDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Travel Date") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, "Date")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = OutlinedTextFieldDefaults.colors()
            )

            // Search Button
            Button(
                onClick = onSearchClick,
                enabled = !isLoading && origin.isNotEmpty() && destination.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Schedules")
                }
            }
        }
    }

    // Date Picker Dialog Logic
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateChange(Date(it))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}