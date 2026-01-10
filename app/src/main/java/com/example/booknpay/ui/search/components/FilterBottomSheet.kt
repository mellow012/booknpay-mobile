package com.example.booknpay.ui.search.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknpay.ui.search.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sortBy: SortOption,
    priceRange: ClosedFloatingPointRange<Float>,
    onSortChange: (SortOption) -> Unit,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sort Options
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            SortOption.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = sortBy == option,
                            onClick = { onSortChange(option) }
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = sortBy == option,
                        onClick = { onSortChange(option) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (option) {
                            SortOption.DEPARTURE_TIME -> "Departure Time"
                            SortOption.PRICE_LOW_TO_HIGH -> "Price: Low to High"
                            SortOption.PRICE_HIGH_TO_LOW -> "Price: High to Low"
                            SortOption.AVAILABLE_SEATS -> "Available Seats"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Range
            Text(
                text = "Price Range",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "MWK ${priceRange.start.toInt()} - MWK ${priceRange.endInclusive.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RangeSlider(
                value = priceRange,
                onValueChange = onPriceRangeChange,
                valueRange = 0f..100000f,
                steps = 10
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Apply Button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
