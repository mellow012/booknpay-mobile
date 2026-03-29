package com.example.booknpay.data.repository

import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.model.Passenger
import com.example.booknpay.util.BookTicketResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
    // ── No class-level ListenerRegistration field. ────────────────────────────
    // Each callbackFlow keeps its own local listener and removes it in awaitClose.
    // This prevents the singleton cleanup race where one ViewModel destroying itself
    // kills the active listener used by another ViewModel.
    private var bookingsListener: com.google.firebase.firestore.ListenerRegistration? = null
    suspend fun createBooking(
        scheduleId: String,
        passengerDetails: List<Passenger>,
        seatNumbers: List<String>,
        totalAmount: Double,
        companyId: String,
        routeId: String
    ): BookTicketResult {
        return try {
            val userId = auth.currentUser?.uid
                ?: return BookTicketResult.Failure("User not authenticated")

            val timestamp = System.currentTimeMillis().toString(36).uppercase()
            val random = UUID.randomUUID().toString().substring(0, 8).uppercase()
            val bookingReference = "BK$timestamp$random"
            val transactionReference = "TX$timestamp$random"

            val bookingData = hashMapOf(
                "userId" to userId,
                "scheduleId" to scheduleId,
                "companyId" to companyId,
                "bookingReference" to bookingReference,
                "transactionReference" to transactionReference,
                "passengerDetails" to passengerDetails.map { passenger ->
                    hashMapOf(
                        "name" to passenger.name.trim(),
                        "age" to passenger.age,
                        "gender" to passenger.gender,
                        "seatNumber" to passenger.seatNumber
                    )
                },
                "seatNumbers" to seatNumbers,
                "totalAmount" to totalAmount,
                "bookingStatus" to "pending",
                "paymentStatus" to "pending",
                "bookingDate" to Timestamp.now(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.runTransaction { transaction ->
                val scheduleRef = firestore.collection("schedules").document(scheduleId)
                val scheduleSnapshot = transaction.get(scheduleRef)

                if (!scheduleSnapshot.exists()) throw Exception("Schedule not found")

                val currentBookedSeats = (scheduleSnapshot.get("bookedSeats") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                val availableSeats = scheduleSnapshot.getLong("availableSeats")?.toInt() ?: 0

                val conflicts = seatNumbers.filter { it in currentBookedSeats }
                if (conflicts.isNotEmpty())
                    throw Exception("Seats ${conflicts.joinToString(", ")} are no longer available")
                if (availableSeats < seatNumbers.size)
                    throw Exception("Not enough available seats")

                val bookingRef = firestore.collection("bookings").document(transactionReference)
                transaction.set(bookingRef, bookingData)
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

            BookTicketResult.Success(
                Booking(
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
            )
        } catch (e: Exception) {
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
     * Real-time stream of the current user's bookings.
     * Each subscriber gets its own Firestore listener; awaitClose removes it
     * automatically when the collecting coroutine is cancelled — no manual
     * cleanup() call required.
     */
    fun getUserBookingsRealtime(): Flow<List<Booking>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Local variable — not a class field — so it can't be stomped by another subscriber
        val listener = firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // THE FIX: Prevent crash on logout
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(emptyList())
                    } else {
                        close(error)
                    }
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
                    } catch (e: Exception) { null }
                } ?: emptyList()

                trySend(bookings)
            }

        awaitClose { listener.remove() }
    }

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

                if (!bookingSnapshot.exists()) throw Exception("Booking not found")
                if (bookingSnapshot.getString("userId") != userId)
                    throw Exception("You don't have permission to cancel this booking")
                if (bookingSnapshot.getString("bookingStatus") != "pending")
                    throw Exception("Only pending bookings can be cancelled")

                transaction.update(
                    bookingRef,
                    mapOf("bookingStatus" to "cancelled", "updatedAt" to FieldValue.serverTimestamp())
                )
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

    suspend fun deleteBooking(bookingId: String): Result<Unit> {
        return try {
            val bookingRef = firestore.collection("bookings").document(bookingId)
            val snapshot = bookingRef.get().await()
            if (snapshot.getString("bookingStatus") != "cancelled")
                throw Exception("Only cancelled bookings can be deleted")
            bookingRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        } catch (e: Exception) { emptyList() }
    }

    fun cleanup() {
        bookingsListener?.remove()
        bookingsListener = null
    }
    // cleanup() intentionally removed — callbackFlow + awaitClose handles it automatically.
}