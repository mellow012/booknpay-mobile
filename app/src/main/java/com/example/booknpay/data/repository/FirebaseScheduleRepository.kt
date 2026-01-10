package com.example.booknpay.data.repository


import com.example.booknpay.data.model.Schedule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseScheduleRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private var schedulesListener: ListenerRegistration? = null

    /**
     * Get schedules with real-time updates
     * Filters out expired schedules (arrival time in the past)
     */
    fun getSchedulesRealtime(): Flow<List<Schedule>> = callbackFlow {
        val now = Timestamp.now()

        schedulesListener = firestore.collection("schedules")
            .whereEqualTo("status", "active")
            .whereGreaterThan("departureDateTime", now)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val schedules = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Schedule(
                            id = doc.id,
                            companyId = doc.getString("companyId") ?: "",
                            busId = doc.getString("busId") ?: "",
                            routeId = doc.getString("routeId") ?: "",
                            departureDateTime = doc.getTimestamp("departureDateTime") ?: Timestamp.now(),
                            arrivalDateTime = doc.getTimestamp("arrivalDateTime") ?: Timestamp.now(),
                            price = doc.getDouble("price") ?: 0.0,
                            availableSeats = doc.getLong("availableSeats")?.toInt() ?: 0,
                            isActive = doc.getBoolean("isActive") ?: true,
                            status = doc.getString("status") ?: "active",
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                            updatedAt = doc.getTimestamp("updatedAt") ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                // Additional client-side filtering for expired schedules
                val activeSchedules = schedules.filter { schedule ->
                    val arrivalDate = schedule.arrivalDateTime?.toDate() ?: return@filter false
                    arrivalDate.after(java.util.Date())
                }

                trySend(activeSchedules)
            }

        awaitClose {
            schedulesListener?.remove()
        }
    }

    /**
     * Get schedules for a specific company with real-time updates
     */
    fun getCompanySchedulesRealtime(companyId: String): Flow<List<Schedule>> = callbackFlow {
        val now = Timestamp.now()

        schedulesListener = firestore.collection("schedules")
            .whereEqualTo("companyId", companyId)
            .whereEqualTo("status", "active")
            .whereGreaterThan("departureDateTime", now)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Firestore schedules listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                println("Firestore schedules listener snapshot: $snapshot")
                val count = snapshot?.size() ?: 0
                println("Firestore schedules listener count: $count")
                val schedules = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Schedule(
                            id = doc.id,
                            companyId = doc.getString("companyId") ?: "",
                            busId = doc.getString("busId") ?: "",
                            routeId = doc.getString("routeId") ?: "",
                            departureDateTime = doc.getTimestamp("departureDateTime") ?: Timestamp.now(),
                            arrivalDateTime = doc.getTimestamp("arrivalDateTime") ?: Timestamp.now(),
                            price = doc.getDouble("price") ?: 0.0,
                            availableSeats = doc.getLong("availableSeats")?.toInt() ?: 0,
                            isActive = doc.getBoolean("isActive") ?: true,
                            status = doc.getString("status") ?: "active",
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                            updatedAt = doc.getTimestamp("updatedAt") ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(schedules)
            }

        awaitClose {
            schedulesListener?.remove()
        }
    }

    /**
     * Fetch schedules once (for initial load)
     */
    suspend fun getSchedules(): List<Schedule> {
        return try {
            val now = Timestamp.now()

            firestore.collection("schedules")
                .whereEqualTo("status", "active")
                .whereGreaterThan("arrivalDateTime", now)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        Schedule(
                            id = doc.id,
                            companyId = doc.getString("companyId") ?: "",
                            busId = doc.getString("busId") ?: "",
                            routeId = doc.getString("routeId") ?: "",
                            departureDateTime = doc.getTimestamp("departureDateTime") ?: Timestamp.now(),
                            arrivalDateTime = doc.getTimestamp("arrivalDateTime") ?: Timestamp.now(),
                            price = doc.getDouble("price") ?: 0.0,
                            availableSeats = doc.getLong("availableSeats")?.toInt() ?: 0,
                            isActive = doc.getBoolean("isActive") ?: true,
                            status = doc.getString("status") ?: "active",
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                            updatedAt = doc.getTimestamp("updatedAt") ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Listen to seat availability changes for a specific schedule
     * Useful for booking screen to show real-time seat updates
     */
    fun listenToSeatAvailability(scheduleId: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection("schedules")
            .document(scheduleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val availableSeats = snapshot?.getLong("availableSeats")?.toInt() ?: 0
                trySend(availableSeats)
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Clean up listeners
     */
    fun cleanup() {
        schedulesListener?.remove()
    }
}