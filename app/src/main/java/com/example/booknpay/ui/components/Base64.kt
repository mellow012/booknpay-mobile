package com.example.booknpay.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun rememberBase64Image(base64String: String?): ImageBitmap? {
    return remember(base64String) {
        if (base64String == null) return@remember null
        try {
            // Clean the header if it exists (e.g., "data:image/jpeg;base64,")
            val pureBase64 = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else base64String

            val imageBytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}