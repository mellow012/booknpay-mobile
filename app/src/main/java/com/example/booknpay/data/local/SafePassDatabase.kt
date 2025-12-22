package com.example.booknpay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.booknpay.data.model.Departure
import com.example.booknpay.data.model.Route
import com.example.booknpay.data.model.Ticket

@Database(
    entities = [Route::class, Departure::class, Ticket::class],
    version = 1,
    exportSchema = false
)
abstract class SafePassDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun departureDao(): DepartureDao
    abstract fun ticketDao(): TicketDao
}
