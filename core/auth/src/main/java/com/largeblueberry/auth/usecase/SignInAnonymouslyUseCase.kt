package com.largeblueberry.auth.usecase

interface SignInAnonymouslyUseCase {
    suspend operator fun invoke(): Result<Unit>
}