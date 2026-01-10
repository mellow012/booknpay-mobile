package com.example.booknpay.di

import com.example.booknpay.data.repository.AuthRepositoryImp
import com.example.booknpay.data.repository.BookingRepositoryImpl
import com.example.booknpay.data.repository.ScheduleRepositoryImpl
import com.example.booknpay.domain.repository.AuthRepository
import com.example.booknpay.domain.repository.BookingRepository
import com.example.booknpay.domain.repository.ScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImp): AuthRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository
}