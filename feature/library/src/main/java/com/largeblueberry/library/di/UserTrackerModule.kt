package com.largeblueberry.library.di


import com.largeblueberry.domain.AuthGateway
import com.largeblueberry.domain.repository.UserUsageRepository
import com.largeblueberry.domain.usecase.CheckUploadAvailabilityUseCase
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