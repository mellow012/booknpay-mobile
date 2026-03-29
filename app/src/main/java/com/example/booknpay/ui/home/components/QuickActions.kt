package com.example.booknpay.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuickActions(
    modifier: Modifier = Modifier,
    onNavigateToBookings: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToBuses: () -> Unit,

) {
    // We use the Primary color for everything to keep it brand-consistent
    val brandColor = MaterialTheme.colorScheme.primary
    val brandContainer = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionItem(
            icon       = Icons.Default.ConfirmationNumber,
            label      = "My Tickets", // "Tickets" is often clearer for passengers
            onClick    = onNavigateToBookings,
            modifier   = Modifier.weight(1f),
            tint       = brandColor,
            background = brandContainer
        )
        QuickActionItem(
            icon       = Icons.Default.AltRoute,
            label      = "Routes",
            onClick    = onNavigateToRoutes,
            modifier   = Modifier.weight(1f),
            tint       = brandColor,
            background = brandContainer
        )
        QuickActionItem(
            icon       = Icons.Default.DirectionsBus,
            label      = "Buses",
            onClick    = onNavigateToBuses,
            modifier   = Modifier.weight(1f),
            tint       = brandColor,
            background = brandContainer
        )
    }
}

@Composable
private fun QuickActionItem(
    icon:       androidx.compose.ui.graphics.vector.ImageVector,
    label:      String,
    onClick:    () -> Unit,
    modifier:   Modifier = Modifier,
    tint:       androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color
) {
    // Using a Surface here for better touch feedback (ripples) and elevation
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(20.dp), // Slightly rounder for a friendlier look
        color    = background,
        onClick  = onClick
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape) // Circle icon containers feel more modern
                    .background(Color.White.copy(alpha = 0.6f)), // Light "pop" behind icon
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = label,
                    tint               = tint,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = tint.copy(alpha = 0.8f) // Slightly softer text color
            )
        }
    }
}