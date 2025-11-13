package com.largeblueberry.auth.di

import com.largeblueberry.auth.usecase.SignInAnonymouslyUseCase
import com.largeblueberry.auth.usecase.SignInAnonymouslyUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

// UseCase를 제공하는 DI 모듈에 추가
@Module
@InstallIn(ViewModelComponent::class) // ViewModel 생명주기에 맞게 설정
abstract class UseCaseModule {
    @Binds
    abstract fun bindSignInAnonymouslyUseCase(
        impl: SignInAnonymouslyUseCaseImpl
    ): SignInAnonymouslyUseCase



    // ... 다른 UseCase 바인딩 ...
}
