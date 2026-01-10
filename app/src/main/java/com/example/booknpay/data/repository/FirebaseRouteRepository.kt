package com.example.booknpay.data.repository

import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.Stop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRouteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val routesCollection = firestore.collection("routes")

// Get all routes (cached for performance)
suspend fun getRoutes(): List<Route> {
    return try {
        val snapshot = routesCollection
            // .whereEqualTo("isActive", true)  // Comment out for testing - add back later
            .get()
            .await()

        val count = snapshot.size()
        println("DEBUG FirebaseRouteRepo: Fetched $count routes from Firestore")

        snapshot.documents.mapNotNull { doc ->
            try {
                // Handle stops as array from Firestore, then convert to JSON string
                val stopsData = doc.get("stops") as? List<Map<*, *>> ?: emptyList()
                val stops = stopsData.mapNotNull { map ->
                    try {
                        Stop(
                            id = map["id"] as? String ?: "",
                            name = map["name"] as? String ?: "",
                            order = (map["order"] as? Number)?.toInt() ?: 0,
                            distanceFromOrigin = (map["distanceFromOrigin"] as? Number)?.toInt()
                                ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                val stopsJson = Gson().toJson(stops)  // Convert to JSON string for model

                val route = Route(
                    id = doc.id,
                    origin = doc.getString("origin") ?: "",
                    destination = doc.getString("destination") ?: "",
                    distance = doc.getLong("distance")?.toInt() ?: 0,
                    duration = doc.getLong("duration")?.toInt() ?: 0,
                    companyId = doc.getString("companyId") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    stopsJson = stopsJson,  // Now a proper string
                    createdAt = doc.getTimestamp("createdAt"),
                    updatedAt = doc.getTimestamp("updatedAt")
                )

                println("DEBUG FirebaseRouteRepo: Parsed route ID=${doc.id} | Origin=${route.origin} | Dest=${route.destination}")
                route
            } catch (e: Exception) {
                println("DEBUG FirebaseRouteRepo: Failed to parse route ${doc.id}: ${e.message}")
                null
            }
        }
    } catch (e: Exception) {
        println("DEBUG FirebaseRouteRepo: Query failed: ${e.message}")
        emptyList()
    }
}

    // Get route by ID (updated similarly for consistency)
    suspend fun getRouteById(routeId: String): Route? {
        return try {
            val doc = routesCollection.document(routeId).get().await()
            if (doc.exists()) {
                // Same stops handling as above
                val stopsData = doc.get("stops") as? List<Map<*, *>> ?: emptyList()
                val stops = stopsData.mapNotNull { map ->
                    try {
                        Stop(
                            id = map["id"] as? String ?: "",
                            name = map["name"] as? String ?: "",
                            order = (map["order"] as? Number)?.toInt() ?: 0,
                            distanceFromOrigin = (map["distanceFromOrigin"] as? Number)?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                val stopsJson = Gson().toJson(stops)

                Route(
                    id = doc.id,
                    origin = doc.getString("origin") ?: "",
                    destination = doc.getString("destination") ?: "",
                    distance = doc.getLong("distance")?.toInt() ?: 0,
                    duration = doc.getLong("duration")?.toInt() ?: 0,
                    companyId = doc.getString("companyId") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true,
                    stopsJson = stopsJson,
                    createdAt = doc.getTimestamp("createdAt"),
                    updatedAt = doc.getTimestamp("updatedAt")
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
