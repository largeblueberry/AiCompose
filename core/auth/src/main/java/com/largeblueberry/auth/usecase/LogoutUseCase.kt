package com.largeblueberry.auth.usecase

interface LogoutUseCase {
    suspend operator fun invoke(): Result<Unit>
}