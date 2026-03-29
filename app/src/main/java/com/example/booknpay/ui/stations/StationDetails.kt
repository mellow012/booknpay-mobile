package com.example.booknpay.ui.stations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknpay.data.model.Company
import com.example.booknpay.data.model.Station
import com.example.booknpay.ui.company.CompanyListItem


@Composable
fun StationDetailScreen(
    station: Station,
    companies: List<Company>, // Pass companies that operate here
    onCompanyClick: (Company) -> Unit,
    onGetDirections: (Double, Double) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // --- Station Header & Map Action ---
        item {
            StationHeader(station, onGetDirections)
        }

        // --- Facilities Section ---
        item {
            Text("Available Facilities", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(station.facilities) { facility ->
                    AssistChip(
                        onClick = {},
                        label = { Text(facility) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        // --- Companies Loading Here ---
        item {
            Text("Operators at this Terminal", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
        }

        items(companies) { company ->
            // Reusing your existing CompanyListItem component!
            CompanyListItem(company = company, onClick = { onCompanyClick(company) })
        }
    }
}

@Composable
fun StationHeader(station: Station, onGetDirections: (Double, Double) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.DarkGray)) {
        // Here you would use a real map snapshot or image
        Column(
            Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text(station.name, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Button(
                onClick = { onGetDirections(station.latitude, station.longitude) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Navigation, null)
                Spacer(Modifier.width(8.dp))
                Text("Directions")
            }
        }
    }
}