package com.example.booknpay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.booknpay.data.model.Booking
import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.Schedule
import com.example.booknpay.util.Converters

@Database(
    entities = [Route::class, Schedule::class, Booking::class],
    version = 4, // Incremented to 4 to handle bookedSeats field and TypeConverter fixes
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SafePassDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun bookingDao(): BookingDao
}