package com.example.booknpay.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknpay.ui.theme.Elevation

private val BrandIndigo   = Color(0xFF2B3896)
private val OrangeSelected = Color(0xFFFF8C00)

private sealed class SeatCell {
    data class Seat(val label: String, val isBooked: Boolean, val isSelected: Boolean) : SeatCell()
    object Aisle  : SeatCell()
    object Driver : SeatCell()
    object Empty  : SeatCell()
}

// Build 2+aisle+2 grid with L/U prefix  (L1..L24 or U1..U24)
private fun buildGrid(
    rows: Int, prefix: String,
    booked: List<String>, selected: List<String>
): List<SeatCell> {
    val cells = mutableListOf<SeatCell>()
    // Header row: empty | empty | aisle | empty | driver
    cells += listOf(SeatCell.Empty, SeatCell.Empty, SeatCell.Aisle, SeatCell.Empty, SeatCell.Driver)
    for (r in 1..rows) {
        val col1 = "${prefix}${(r - 1) * 2 + 1}"   // e.g. L1, L3, L5 …
        val col2 = "${prefix}${(r - 1) * 2 + 2}"   // e.g. L2, L4, L6 …
        val col3 = "${prefix}${(r - 1) * 2 + 1 + rows * 2}"  // right side
        val col4 = "${prefix}${(r - 1) * 2 + 2 + rows * 2}"
        cells += SeatCell.Seat(col1, booked.contains(col1), selected.contains(col1))
        cells += SeatCell.Seat(col2, booked.contains(col2), selected.contains(col2))
        cells += SeatCell.Aisle
        cells += SeatCell.Seat(col3, booked.contains(col3), selected.contains(col3))
        cells += SeatCell.Seat(col4, booked.contains(col4), selected.contains(col4))
    }
    return cells
}

@Composable
fun SeatSelectionScreen(
    state:       BookingState,
    onSeatClick: (String) -> Unit,
    onProceed:   () -> Unit
) {
    var deckIndex by remember { mutableIntStateOf(0) }
    val decks     = listOf("Lower Deck" to "L", "Upper Deck" to "U")
    val prefix    = decks[deckIndex].second

    val booked   = state.scheduleDetails?.schedule?.bookedSeats ?: emptyList()
    val rows     = 12  // 12 rows × 4 cols = 48 seats per deck
    val grid     = remember(deckIndex, state.selectedSeats) {
        buildGrid(rows, prefix, booked, state.selectedSeats)
    }

    val firstSelected = state.selectedSeats.firstOrNull()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 90.dp)) {

            Spacer(Modifier.height(4.dp))

            // ── Deck switcher ──────────────────────────────────────────────
            Surface(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                RoundedCornerShape(50),
                MaterialTheme.colorScheme.surfaceVariant.copy(.5f)
            ) {
                Row(Modifier.padding(4.dp)) {
                    decks.forEachIndexed { i, (label, _) ->
                        val active = i == deckIndex
                        Surface(
                            onClick = { deckIndex = i },
                            shape   = RoundedCornerShape(50),
                            color   = if (active) Color.White else Color.Transparent,
                            modifier = Modifier.weight(1f),
                            shadowElevation = if (active) 2.dp else 0.dp
                        ) {
                            Text(label,
                                Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (active) BrandIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Bus shell card ─────────────────────────────────────────────
            Card(Modifier.fillMaxWidth().weight(1f), RoundedCornerShape(20.dp),
                CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(.35f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {

                    // Legend
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        Arrangement.SpaceEvenly, Alignment.CenterVertically
                    ) {
                        LegendItem(Color(0xFFE8E8EE), Color.Transparent, "AVAILABLE", false)
                        LegendItem(OrangeSelected, OrangeSelected, "SELECTED", false)
                        LegendItem(Color(0xFFB0B0C0), Color.Transparent, "BOOKED", true)
                    }

                    // Seat grid — 5 columns (seat|seat|aisle|seat|seat)
                    LazyVerticalGrid(
                        columns               = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.weight(1f)
                    ) {
                        items(grid) { cell ->
                            when (cell) {
                                is SeatCell.Seat -> SeatTile(
                                    label     = cell.label,
                                    isBooked  = cell.isBooked,
                                    isSelected = cell.isSelected,
                                    onClick   = { if (!cell.isBooked) onSeatClick(cell.label) }
                                )
                                is SeatCell.Aisle  -> Spacer(Modifier.size(44.dp))
                                is SeatCell.Driver -> Box(
                                    Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                                        .background(BrandIndigo.copy(.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Default.DriveFileMove, "Driver",
                                        tint = BrandIndigo.copy(.55f), modifier = Modifier.size(22.dp))
                                    Text("DRIVER", fontSize = 7.sp, color = BrandIndigo.copy(.55f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp))
                                }
                                is SeatCell.Empty  -> Spacer(Modifier.size(44.dp))
                            }
                        }
                    }

                    // Amenities row + ENTRY label
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AMENITIES", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp, fontSize = 10.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf(Icons.Default.Wifi, Icons.Default.BatteryChargingFull,
                                    Icons.Default.AcUnit, Icons.Default.WaterDrop).forEach { ic ->
                                    Icon(ic, null, Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ENTRY", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp, fontSize = 10.sp)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(3.dp).height(24.dp).clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(.35f)))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Safe travels card ──────────────────────────────────────────
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp),
                CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Safe Travels Commitment",
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Each seat is sanitized before every journey. Our premium express coaches feature GPS tracking and emergency assistance for your peace of mind.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Sticky bottom bar ──────────────────────────────────────────────
        Surface(Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text("SELECTED SEAT", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp, fontSize = 10.sp)
                        Text(
                            if (firstSelected != null) "$firstSelected / ${decks[deckIndex].first}" else "None",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold, color = BrandIndigo
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOTAL FARE", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp, fontSize = 10.sp)
                        Text("MWK ${"%,.0f".format(state.totalAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold, color = BrandIndigo)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick  = onProceed,
                    enabled  = state.selectedSeats.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                ) {
                    Text("Proceed to Payment", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

// ─── Seat tile ────────────────────────────────────────────────────────────────
@Composable
private fun SeatTile(label: String, isBooked: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val bg = when {
        isBooked   -> Color(0xFFB0B0C0).copy(.45f)
        isSelected -> OrangeSelected
        else       -> Color(0xFFE8E8EE)
    }
    val txtColor = when {
        isBooked   -> Color.Gray.copy(.5f)
        isSelected -> Color.White
        else       -> Color(0xFF3A3A5C)
    }

    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(bg)
            .clickable(enabled = !isBooked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, color = txtColor, fontSize = 11.sp)
    }
}

// ─── Legend item ──────────────────────────────────────────────────────────────
@Composable
private fun LegendItem(color: Color, borderColor: Color, label: String, strikethrough: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(color)
                .then(if (borderColor != Color.Transparent) Modifier.border(1.dp, borderColor, RoundedCornerShape(4.dp)) else Modifier)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp, letterSpacing = 0.5.sp)
    }
}