package com.largeblueberry.library.di

import com.largeblueberry.usertracker.repository.AuthGateway
import com.largeblueberry.usertracker.repository.UserUsageRepository
import com.largeblueberry.usertracker.usecase.CheckUploadAvailabilityUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UserTrackerModule {

    @Provides
    @ViewModelScoped
    fun provideCheckUploadAvailabilityUseCase(
        userUsageRepository: UserUsageRepository,
        authGateway: AuthGateway
    ): CheckUploadAvailabilityUseCase {
        return CheckUploadAvailabilityUseCase(
            userUsageRepository = userUsageRepository,
            authGateway = authGateway
        )
    }



}