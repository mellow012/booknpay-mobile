package com.example.booknpay.ui.ticket

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

private val BrandIndigo = Color(0xFF2B3896)
private val BrandDeep = Color(0xFF1A1D6E)

private suspend fun makeQr(content: String, size: Int = 512): Bitmap? = withContext(Dispatchers.Default) {
    runCatching {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, if (matrix[x, y]) Color.Black.toArgb() else Color.White.toArgb())
                }
            }
        }
    }.getOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(
    bookingReference: String?,
    onDone: () -> Unit,
    viewModel: MyTicketsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val booking = state.bookings.find { it.bookingReference == bookingReference }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(bookingReference) {
        if (bookingReference != null) {
            qrBitmap = makeQr("TIBHUKEBUS-$bookingReference")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = BrandIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "TibhukeBus",
                            fontWeight = FontWeight.Black,
                            color = BrandIndigo
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            booking == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎫", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(16.dp))
                        Text("Ticket not found", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onDone) {
                            Text("Go Back")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val isPaid = booking.paymentStatus == "paid"
                    val dateFmt = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                    val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                    val dateStr = booking.bookingDate?.toDate()?.let { dateFmt.format(it) } ?: "—"

                    // Active Status Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPaid) Color(0xFF1B8A4E).copy(alpha = 0.1f) else Color(0xFFFF9800).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (isPaid) Color(0xFF1B8A4E) else Color(0xFFFF9800))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isPaid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPaid) Color(0xFF1B8A4E) else Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (isPaid) "Ticket is active and ready for boarding"
                                else "Payment pending — please complete payment to activate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPaid) Color(0xFF1B8A4E) else Color(0xFFFF9800)
                            )
                        }
                    }

                    // Main Ticket Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandIndigo)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "INTERCITY EXPRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "Trip #${booking.bookingReference.takeLast(6)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(Modifier.height(24.dp))

                            // Timeline
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("DEPARTURE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                    Text(
                                        "09:45 AM",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Row(Modifier.weight(1f).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f), thickness = 2.dp)
                                    Icon(Icons.Default.DirectionsBus, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f), thickness = 2.dp)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ARRIVAL", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                    Text(
                                        "02:15 PM",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // QR Code Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Show this QR code to the conductor",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap!!.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    CircularProgressIndicator()
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                "TXID: TIB-${booking.bookingReference.takeLast(8).uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* TODO: Download PDF */ },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandDeep)
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Download PDF", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { /* TODO: Share */ },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Share", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Travel Instructions
                    Column {
                        Text(
                            "Travel Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandIndigo
                        )
                        Spacer(Modifier.height(12.dp))

                        listOf(
                            "Arrive at least 15 minutes before departure.",
                            "Keep a valid ID ready for verification.",
                            "No refunds after departure."
                        ).forEach { instruction ->
                            Row(Modifier.padding(vertical = 6.dp)) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = BrandIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    instruction,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun TicketDetail(
    label: String, value: String,
    valueColor: Color = Color.Unspecified,
    align: TextAlign = TextAlign.Start
) {
    Column(horizontalAlignment = if (align == TextAlign.End) Alignment.End else Alignment.Start) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
            textAlign = align)
    }
}