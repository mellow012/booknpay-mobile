package com.example.booknpay.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.booknpay.data.local.DepartureDao
import com.example.booknpay.data.local.RouteDao
import com.example.booknpay.data.local.SafePassDatabase
import com.example.booknpay.data.local.TicketDao
import com.example.booknpay.data.model.Departure
import com.example.booknpay.data.model.Route
import com.example.booknpay.data.repository.DepartureRepositoryImpl
import com.example.booknpay.data.repository.TicketRepositoryImpl
import com.example.booknpay.domain.repository.DepartureRepository
import com.example.booknpay.domain.repository.TicketRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSafePassDatabase(
        @ApplicationContext context: Context,
        // We need to use a Provider to break the dependency cycle
        routeDaoProvider: Provider<RouteDao>,
        departureDaoProvider: Provider<DepartureDao>
    ): SafePassDatabase {
        return Room.databaseBuilder(
            context,
            SafePassDatabase::class.java,
            "safepass_db"
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate the database with sample data
                CoroutineScope(Dispatchers.IO).launch {
                    // Routes
                    routeDaoProvider.get().insertRoutes(listOf(
                        Route(1, "Blantyre", "Lilongwe"),
                        Route(2, "Lilongwe", "Mzuzu"),
                        Route(3, "Blantyre", "Zomba")
                    ))

                    // Departures
                    departureDaoProvider.get().insertDepartures(listOf(
                        Departure(1, 1, "AXA Coach", 20, 0, 15000),
                        Departure(2, 1, "SOS Coach", 25, 0, 14000),
                        Departure(3, 2, "Tawonga Coach", 15, 0, 18000)
                    ))
                }
            }
        })
        .build()
    }

    @Provides
    @Singleton
    fun provideRouteDao(database: SafePassDatabase): RouteDao = database.routeDao()

    @Provides
    @Singleton
    fun provideDepartureDao(database: SafePassDatabase): DepartureDao = database.departureDao()

    @Provides
    @Singleton
    fun provideTicketDao(database: SafePassDatabase): TicketDao = database.ticketDao()

    @Provides
    @Singleton
    fun provideDepartureRepository(departureDao: DepartureDao): DepartureRepository {
        return DepartureRepositoryImpl(departureDao)
    }

    @Provides
    @Singleton
    fun provideTicketRepository(departureDao: DepartureDao, ticketDao: TicketDao): TicketRepository {
        return TicketRepositoryImpl(departureDao, ticketDao)
    }
}
