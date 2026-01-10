package com.example.booknpay.di



import com.example.booknpay.domain.use_case.validation.ValidateEmailUseCase
import com.example.booknpay.domain.use_case.validation.ValidateNameUseCase
import com.example.booknpay.domain.use_case.validation.ValidatePasswordUseCase
import com.example.booknpay.domain.use_case.validation.ValidatePhoneUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideValidateEmailUseCase(): ValidateEmailUseCase {
        return ValidateEmailUseCase()
    }

    @Provides
    @Singleton
    fun provideValidatePasswordUseCase(): ValidatePasswordUseCase {
        return ValidatePasswordUseCase()
    }

    @Provides
    @Singleton
    fun provideValidateNameUseCase(): ValidateNameUseCase {
        return ValidateNameUseCase()
    }

    @Provides
    @Singleton
    fun provideValidatePhoneUseCase(): ValidatePhoneUseCase {
        return ValidatePhoneUseCase()
    }
}