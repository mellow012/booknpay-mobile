package com.example.booknpay.util

import androidx.room.TypeConverter
import com.example.booknpay.data.model.Passenger
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.seconds
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it, 0) }
    }

    @TypeConverter
    fun fromPassengerList(value: String?): List<Passenger> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<Passenger>>() {}.type
        return gson.fromJson<List<Passenger>>(value, listType)
    }


    @TypeConverter
    fun toPassengerList(list: List<Passenger>?): String {
        // Explicitly provide the type to emptyList
        return gson.toJson(list ?: emptyList<Passenger>())
    }


    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson<List<String>>(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        // Explicitly provide the type to emptyList
        return gson.toJson(list ?: emptyList<String>())
    }

    companion object {
        /**
         * Utility to format timestamps for the UI.
         * Accessible via Converters.formatTimestamp(ts)
         */
        fun formatTimestamp(timestamp: Timestamp?): String {
            if (timestamp == null) return "--:--"
            return try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(timestamp.toDate())
            } catch (e: Exception) {
                "--:--"
            }
        }

        fun formatPrice(price: Double): String {
            return java.text.NumberFormat.getIntegerInstance(Locale.US).format(price)
        }
        fun formatDateForUI(millis: Long?): String {
            if (millis == null) return "Select Date"
            return try {
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                sdf.format(Date(millis))
            } catch (e: Exception) {
                "Select Date"
            }
        }

        /**
         * Converts selected millis from DatePicker back to Firebase Timestamp
         */
        fun millisToTimestamp(millis: Long?): com.google.firebase.Timestamp? {
            return millis?.let { com.google.firebase.Timestamp(it / 1000, 0) }
        }
    }

}

fun getTimeBasedGreeting(): Pair<String, String> {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Mwadzuka bwanji" to "Good Morning"
        in 12..16 -> "Mwaswera bwanji" to "Good Afternoon"
        else -> "Mwaswera bwanji" to "Good Evening"
    }
}

