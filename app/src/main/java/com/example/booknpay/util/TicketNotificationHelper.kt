package com.example.booknpay.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.booknpay.MainActivity
import com.example.booknpay.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TicketNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context // Hilt now knows how to provide this
)
 {

    private val channelId = "booking_updates"

    // Remove the OREO check if your minSdk is 26+
    init {
        val name = "Booking Updates"
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showBookingApprovedNotification(bookingRef: String) {
        // Check for Android 13+ Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return // Or handle the rejection
            }
        }

        // Wrap in try-catch to satisfy the "SecurityException" warning
        try {
            val builder = NotificationCompat.Builder(context, channelId)
            // ... your builder code ...
            NotificationManagerCompat.from(context).notify(bookingRef.hashCode(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}