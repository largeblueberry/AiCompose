package com.largeblueberry.aicompose.library.domainLayer.model

/**
 * 오디오 녹음 파일의 도메인 모델
 *
 * 도메인 모델에 ID를 포함한 설계 근거:
 * 1. 개발 효율성: filepath 대신 ID로 식별하여 코드 간소화
 * 2. 성능: ID 검색(O(1)) vs filepath 검색(O(n))
 * 3. 안정성: ID는 불변, filepath는 가변적 특성
 * 4. 도메인 관점: 정체성(Identity)은 객체의 본질적 특성
 * 5. 기술 중립성: Int 타입으로 특정 DB 기술에 비의존적
 */
data class LibraryModel(
    val id: Int = 0,
    val filename: String,
    val filePath: String,
    val fileSize: Long,
    val duration: String,
    val createdAt: Long = System.currentTimeMillis()
)
