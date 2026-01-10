package com.example.booknpay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                ButtonContent(text, loading, icon)
            }
        }
        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier.height(56.dp)
            ) {
                ButtonContent(text, loading, icon)
            }
        }
        ButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier
            ) {
                ButtonContent(text, loading, icon)
            }
        }
    }
}

@Composable
private fun ButtonContent(text: String, loading: Boolean, icon: ImageVector?) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text)
        }
    }
}

enum class ButtonVariant {
    Primary, Secondary, Text
}
