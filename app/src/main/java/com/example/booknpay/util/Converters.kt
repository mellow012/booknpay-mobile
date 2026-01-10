package com.example.booknpay.util

import androidx.room.TypeConverter
import com.example.booknpay.data.model.Passenger
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
}
