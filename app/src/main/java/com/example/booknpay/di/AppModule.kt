package com.example.booknpay.di

import android.content.Context
import androidx.room.Room
import com.example.booknpay.data.local.BookingDao
import com.example.booknpay.data.local.RouteDao
import com.example.booknpay.data.local.SafePassDatabase
import com.example.booknpay.data.local.ScheduleDao
import com.example.booknpay.data.repository.FirebaseBookingRepository
import com.example.booknpay.data.repository.FirebaseRouteRepository
import com.example.booknpay.data.repository.FirebaseScheduleRepository
import com.example.booknpay.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideSafePassDatabase(@ApplicationContext context: Context): SafePassDatabase {
        return Room.databaseBuilder(
            context,
            SafePassDatabase::class.java,
            "safepass_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideRouteDao(database: SafePassDatabase): RouteDao = database.routeDao()

    @Provides
    @Singleton
    fun provideScheduleDao(database: SafePassDatabase): ScheduleDao = database.scheduleDao()

    @Provides
    @Singleton
    fun provideBookingDao(database: SafePassDatabase): BookingDao = database.bookingDao()

    @Provides
    @Singleton
    fun provideNotificationRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): NotificationRepository {
        return NotificationRepository(firestore, auth)
    }


    @Provides
    @Singleton
    fun provideFirebaseScheduleRepository(firestore: FirebaseFirestore): FirebaseScheduleRepository {
        return FirebaseScheduleRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseBookingRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): FirebaseBookingRepository {
        return FirebaseBookingRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideFirebaseRouteRepository(firestore: FirebaseFirestore): FirebaseRouteRepository {
        return FirebaseRouteRepository(firestore)
    }
}



