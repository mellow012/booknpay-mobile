package com.example.booknpay.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCity(): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Try to get a fresh location fix first
            val location = client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).await()
            // 2. Fallback to last known location if fresh fix fails (common if GPS is weak)
                ?: client.lastLocation.await()

            location?.let {
                // Reverse geocode to get the city name
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val city = addresses?.firstOrNull()?.let { addr ->
                    addr.locality ?: addr.adminArea ?: addr.subAdminArea
                }

                // Optional: Map common Malawian names if Geocoder returns long strings
                return@withContext when(city) {
                    "City of Lilongwe" -> "Lilongwe"
                    "City of Blantyre" -> "Blantyre"
                    else -> city
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}