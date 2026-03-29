package com.example.booknpay.domain.repository

import com.example.booknpay.data.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private var notificationsListener: ListenerRegistration? = null

    /**
     * Get real-time notifications for current user
     * Matches web's notification flow
     */
    fun getUserNotificationsRealtime(): Flow<List<Notification>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            // Don't close(error), just wait for a new user or a manual close
            return@callbackFlow
        }
        val listener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Check if it's a logout-related permission error to prevent crashing
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(emptyList())
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }

                // Create the list explicitly to avoid "unresolved reference"
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Notification(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            type = doc.getString("type") ?: "system",
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            // Ensure 'data' mapping is safe
                            data = (doc.get("data") as? Map<*, *>)
                                ?.mapKeys { it.key.toString() }
                                ?.mapValues { it.value.toString() } ?: emptyMap(),
                            actionUrl = doc.getString("actionUrl"),
                            priority = doc.getString("priority") ?: "medium",
                            isRead = doc.getBoolean("isRead") ?: false,
                            readAt = doc.getTimestamp("readAt"),
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null // Skip documents that don't match the format
                    }
                } ?: emptyList()

                trySend(items) // Send the 'items' list
            }
        notificationsListener = listener


        fun cleanup(){
            notificationsListener?.remove()
            notificationsListener = null
        }
        awaitClose {
            listener.remove()
            notificationsListener = null // Keep your reference clean
        }
    }

    /**
     * Mark notification as read
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .update(mapOf(
                    "isRead" to true,
                    "readAt" to FieldValue.serverTimestamp()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            val batch = firestore.batch()

            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "isRead" to true,
                    "readAt" to FieldValue.serverTimestamp()
                ))
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete notification
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all notifications for current user
     */
    suspend fun clearAll(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            val batch = firestore.batch()

            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get unread count
     */
    suspend fun getUnreadCount(): Int {
        return try {
            val userId = auth.currentUser?.uid ?: return 0

            firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Clean up listeners
     */
    fun cleanup() {
        notificationsListener?.remove()
    }
}

/**
 * Notification templates matching web app
 */
object NotificationTemplates {
    fun bookingConfirmed(bookingId: String, route: String) = mapOf(
        "type" to "booking",
        "title" to "Booking Confirmed! 🎉",
        "message" to "Your booking for $route has been confirmed.",
        "data" to mapOf("bookingId" to bookingId, "url" to "/bookings/$bookingId"),
        "priority" to "high"
    )

    fun paymentReceived(amount: Double, bookingId: String) = mapOf(
        "type" to "payment",
        "title" to "Payment Received",
        "message" to "We've received your payment of MWK ${amount.toLong().toString()}.",
        "data" to mapOf("bookingId" to bookingId, "url" to "/bookings/$bookingId"),
        "priority" to "medium"
    )

    fun scheduleChanged(route: String, newTime: String) = mapOf(
        "type" to "schedule",
        "title" to "Schedule Update",
        "message" to "The departure time for $route has been changed to $newTime.",
        "priority" to "high"
    )

    fun bookingApproved(bookingId: String, companyName: String) = mapOf(
        "type" to "booking",
        "title" to "Booking Approved",
        "message" to "Your booking has been approved by $companyName. Complete payment to secure your seats.",
        "data" to mapOf("bookingId" to bookingId, "url" to "/bookings"),
        "priority" to "high"
    )
}