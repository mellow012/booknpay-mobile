package com.example.booknpay.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MarqueeDefaults.Spacing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknpay.ui.theme.CardBackground
import com.example.booknpay.ui.theme.Elevation


@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevated: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (elevated) Elevation.Small else 0.dp
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (!elevated) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}
