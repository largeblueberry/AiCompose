package com.largeblueberry.auth.usecase

import com.largeblueberry.auth.repository.AuthRepository
import javax.inject.Inject

class SignInAnonymouslyUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : SignInAnonymouslyUseCase {
    override suspend operator fun invoke(): Result<Unit> {
        // authRepository.signInAnonymously()가 반환하는 Result<String>을
        // .map을 사용해 Result<Unit>으로 변환합니다.
        return authRepository.signInAnonymously().map { /* 성공 시 String(UID) 값은 무시하고 Unit으로 변환 */ }
    }
}