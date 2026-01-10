package com.example.booknpay.data.repository

import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.model.Passenger
import com.example.booknpay.util.BookTicketResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseBookingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private var bookingsListener: ListenerRegistration? = null

    /**
     * Creates a booking request in Firestore
     * Matches web app structure exactly - no routeId field
     * (routeId is already in the schedule document)
     */
    suspend fun createBooking(
        scheduleId: String,
        passengerDetails: List<Passenger>,
        seatNumbers: List<String>,
        totalAmount: Double,
        companyId: String,
        routeId: String // We receive it but DON'T store it in booking
    ): BookTicketResult {
        return try {
            val userId = auth.currentUser?.uid
                ?: return BookTicketResult.Failure("User not authenticated")

            // Generate unique references (matching web format)
            val timestamp = System.currentTimeMillis().toString(36).uppercase()
            val random = UUID.randomUUID().toString().substring(0, 8).uppercase()
            val bookingReference = "BK$timestamp$random"
            val transactionReference = "TX$timestamp$random"

            // Prepare booking data - EXACTLY matching your web app structure
            val bookingData = hashMapOf(
                // Core fields
                "userId" to userId,
                "scheduleId" to scheduleId,
                "companyId" to companyId,
                // Note: NO routeId here - it's in the schedule already

                // References
                "bookingReference" to bookingReference,
                "transactionReference" to transactionReference,

                // Passenger info
                "passengerDetails" to passengerDetails.map { passenger ->
                    hashMapOf(
                        "name" to passenger.name.trim(),
                        "age" to passenger.age,
                        "gender" to passenger.gender,
                        "seatNumber" to passenger.seatNumber
                    )
                },
                "seatNumbers" to seatNumbers,

                // Payment
                "totalAmount" to totalAmount,
                "bookingStatus" to "pending",
                "paymentStatus" to "pending",

                // Timestamps
                "bookingDate" to Timestamp.now(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            // Use Firestore transaction for atomicity
            firestore.runTransaction { transaction ->
                // Get schedule
                val scheduleRef = firestore.collection("schedules").document(scheduleId)
                val scheduleSnapshot = transaction.get(scheduleRef)

                if (!scheduleSnapshot.exists()) {
                    throw Exception("Schedule not found")
                }

                // Check current booked seats and available count
                val currentBookedSeats = (scheduleSnapshot.get("bookedSeats") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                val availableSeats = scheduleSnapshot.getLong("availableSeats")?.toInt() ?: 0

                // Check for conflicts
                val conflicts = seatNumbers.filter { it in currentBookedSeats }
                if (conflicts.isNotEmpty()) {
                    throw Exception("Seats ${conflicts.joinToString(", ")} are no longer available")
                }

                // Check if enough seats
                if (availableSeats < seatNumbers.size) {
                    throw Exception("Not enough available seats")
                }

                // Create booking document (use transactionReference as document ID)
                val bookingRef = firestore.collection("bookings")
                    .document(transactionReference)
                transaction.set(bookingRef, bookingData)

                // Update schedule: reserve seats
                transaction.update(
                    scheduleRef,
                    mapOf(
                        "bookedSeats" to FieldValue.arrayUnion(*seatNumbers.toTypedArray()),
                        "availableSeats" to FieldValue.increment(-seatNumbers.size.toLong()),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                transactionReference
            }.await()

            // Return success with booking object
            val booking = Booking(
                id = transactionReference,
                bookingReference = bookingReference,
                userId = userId,
                scheduleId = scheduleId,
                companyId = companyId,
                bookingStatus = "pending",
                paymentStatus = "pending",
                totalAmount = totalAmount,
                seatNumbers = seatNumbers,
                passengerDetails = passengerDetails,
                bookingDate = Timestamp.now(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            BookTicketResult.Success(booking)

        } catch (e: Exception) {
            // Parse error messages
            when {
                e.message?.contains("not found", ignoreCase = true) == true ->
                    BookTicketResult.Failure("Schedule not found")

                e.message?.contains("no longer available", ignoreCase = true) == true ->
                    BookTicketResult.SoldOut

                e.message?.contains("Not enough", ignoreCase = true) == true ->
                    BookTicketResult.SoldOut

                e.message?.contains("not authenticated", ignoreCase = true) == true ->
                    BookTicketResult.Failure("Please log in to book")

                else ->
                    BookTicketResult.Failure(e.message ?: "Failed to create booking request")
            }
        }
    }

    /**
     * Get user's bookings with real-time updates
     */
    fun getUserBookingsRealtime(): Flow<List<Booking>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        bookingsListener = firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Booking(
                            id = doc.id,
                            bookingReference = doc.getString("bookingReference") ?: doc.id,
                            userId = doc.getString("userId") ?: "",
                            scheduleId = doc.getString("scheduleId") ?: "",
                            companyId = doc.getString("companyId") ?: "",
                            bookingStatus = doc.getString("bookingStatus") ?: "pending",
                            paymentStatus = doc.getString("paymentStatus") ?: "pending",
                            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                            seatNumbers = (doc.get("seatNumbers") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList(),
                            passengerDetails = parsePassengerDetails(doc.get("passengerDetails")),
                            bookingDate = doc.getTimestamp("bookingDate") ?: Timestamp.now(),
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                            updatedAt = doc.getTimestamp("updatedAt") ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null // Skip malformed documents
                    }
                } ?: emptyList()

                trySend(bookings).isSuccess
            }

        awaitClose {
            bookingsListener?.remove()
        }
    }

    /**
     * Cancel a pending booking
     * Only works if bookingStatus is "pending"
     */
    suspend fun cancelBooking(
        bookingId: String,
        scheduleId: String,
        seatNumbers: List<String>
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not authenticated"))

            firestore.runTransaction { transaction ->
                val bookingRef = firestore.collection("bookings").document(bookingId)
                val bookingSnapshot = transaction.get(bookingRef)

                if (!bookingSnapshot.exists()) {
                    throw Exception("Booking not found")
                }

                // Check if user owns this booking
                val bookingUserId = bookingSnapshot.getString("userId")
                if (bookingUserId != userId) {
                    throw Exception("You don't have permission to cancel this booking")
                }

                val bookingStatus = bookingSnapshot.getString("bookingStatus")

                // Can only cancel if pending
                if (bookingStatus != "pending") {
                    throw Exception("Only pending bookings can be cancelled")
                }

                // Update booking status
                transaction.update(
                    bookingRef,
                    mapOf(
                        "bookingStatus" to "cancelled",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                // Release seats back to schedule
                val scheduleRef = firestore.collection("schedules").document(scheduleId)
                transaction.update(
                    scheduleRef,
                    mapOf(
                        "availableSeats" to FieldValue.increment(seatNumbers.size.toLong()),
                        "bookedSeats" to FieldValue.arrayRemove(*seatNumbers.toTypedArray()),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a cancelled booking (cleanup)
     */
    suspend fun deleteBooking(bookingId: String): Result<Unit> {
        return try {
            val bookingRef = firestore.collection("bookings").document(bookingId)
            val snapshot = bookingRef.get().await()

            val bookingStatus = snapshot.getString("bookingStatus")
            if (bookingStatus != "cancelled") {
                throw Exception("Only cancelled bookings can be deleted")
            }

            bookingRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper to parse passenger details from Firestore
     */
    private fun parsePassengerDetails(data: Any?): List<Passenger> {
        return try {
            (data as? List<*>)?.mapNotNull { item ->
                (item as? Map<*, *>)?.let { map ->
                    Passenger(
                        name = map["name"] as? String ?: "",
                        age = (map["age"] as? Number)?.toInt() ?: 0,
                        gender = map["gender"] as? String ?: "male",
                        seatNumber = map["seatNumber"] as? String
                    )
                }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clean up listeners
     */
    fun cleanup() {
        bookingsListener?.remove()
        bookingsListener = null
    }
}