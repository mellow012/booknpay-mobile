package com.example.booknpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.booknpay.ui.theme.*

@Composable
fun StatusChip(
    text: String,
    status: ChipStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ChipStatus.Success -> Success.copy(alpha = 0.1f) to Success
        ChipStatus.Warning -> Warning.copy(alpha = 0.1f) to Warning
        ChipStatus.Error -> Error.copy(alpha = 0.1f) to Error
        ChipStatus.Info -> Info.copy(alpha = 0.1f) to Info
        ChipStatus.Neutral -> Color.Gray.copy(alpha = 0.1f) to Color.Gray
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

enum class ChipStatus {
    Success, Warning, Error, Info, Neutral
}
