package com.example.booknpay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
    
@Entity(tableName = "routes")
data class Route(
    @PrimaryKey
    val id: String,
    val origin: String,
    val destination: String,
    val distance: Int,
    val duration: Int,
    val companyId: String,
    val isActive: Boolean = true,
    val stopsJson: String = "[]", // Store stops as JSON string (fallback if Firestore has array)
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    // Helper to get stops as list (from JSON string)
    fun getStops(): List<Stop> {
        return try {
            val type = object : TypeToken<List<Stop>>() {}.type
            Gson().fromJson(stopsJson, type) ?: emptyList()
        } catch (e: Exception) {
            println("DEBUG Route: Failed to parse stopsJson: ${e.message}")
            emptyList()
        }
    }

    companion object {
        // Helper to create Route from stops list (serializes to JSON string)
        fun fromStops(
            id: String,
            origin: String,
            destination: String,
            distance: Int,
            duration: Int,
            companyId: String,
            isActive: Boolean = true,
            stops: List<Stop> = emptyList(),
            createdAt: Timestamp? = null,
            updatedAt: Timestamp? = null
        ): Route {
            val stopsJson = Gson().toJson(stops)
            return Route(
                id, origin, destination, distance, duration,
                companyId, isActive, stopsJson, createdAt, updatedAt
            )
        }
    }
}

data class Stop(
    val id: String,
    val name: String,
    val order: Int,
    val distanceFromOrigin: Int
)