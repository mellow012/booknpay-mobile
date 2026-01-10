package com.example.booknpay.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.booknpay.data.model.Booking
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Delete
    suspend fun deleteBooking(booking: Booking): Int

    @Query("SELECT * FROM bookings ORDER BY bookingDate DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId LIMIT 1")
    suspend fun getBookingById(bookingId: String): Booking?

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY bookingDate DESC")
    fun getUserBookings(userId: String): Flow<List<Booking>>

    @Query("DELETE FROM bookings")
    suspend fun deleteAll()
}