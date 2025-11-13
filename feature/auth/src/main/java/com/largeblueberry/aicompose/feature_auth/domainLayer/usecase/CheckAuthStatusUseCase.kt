package com.largeblueberry.aicompose.feature_auth.domainLayer.usecase

import com.largeblueberry.auth.model.UserCore // Domain 모델 임포트
import com.largeblueberry.auth.repository.AuthRepository
import javax.inject.Inject

class CheckAuthStatusUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * 이 UseCase를 실행합니다.
     * 현재 로그인된 사용자 정보를 반환하거나, 로그인되지 않았다면 null을 반환합니다.
     */
    suspend operator fun invoke(): UserCore? { // <- Domain 모델로 변경
        return repository.getCurrentUser()
    }
}