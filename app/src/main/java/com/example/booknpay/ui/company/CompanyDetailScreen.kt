package com.example.booknpay.ui.company

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.booknpay.data.model.Company
import com.example.booknpay.data.model.CompanyService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(
    company: Company,
    viewModel: CompanyViewModel = hiltViewModel(),
    onBookRoute: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val services by viewModel.getCompanyServices(company.id).collectAsState(initial = emptyList())
    val brandColor = Color(android.graphics.Color.parseColor(company.themeColor))
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(company.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Call Support", color = Color.White) },
                icon = { Icon(Icons.Default.Phone, null, tint = Color.White) },
                containerColor = brandColor,
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${company.contact}"))
                    context.startActivity(intent)
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 1. Branding Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(brandColor.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            company.name.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = brandColor
                        )
                        Text(
                            "Official Booking Partner",
                            style = MaterialTheme.typography.labelMedium,
                            color = brandColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 2. Info Row (Branches & Status)
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip(Icons.Default.History, "Daily Trips")
                    InfoChip(Icons.Default.Verified, "Verified")
                    InfoChip(Icons.Default.Route, "${company.branches.size} Branches")
                }
            }

            // 3. Route/Fare Section
            item {
                Text(
                    "Standard Routes & Fares",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(services) { service ->
                ServiceFareCard(service = service, brandColor = brandColor, onBook = { onBookRoute(service.id) })
            }
        }
    }
}

@Composable
fun ServiceFareCard(service: CompanyService, brandColor: Color, onBook: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("${service.origin} to ${service.destination}", fontWeight = FontWeight.ExtraBold)
                    Text(service.busType, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(
                    "MWK ${String.format("%,.0f", service.adultFare)}",
                    color = brandColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = Color.Gray)
                Text(
                    " Departures: ${service.departureTimes.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onBook,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Check Availability")
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, Modifier.size(20.dp), tint = Color.Gray)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}