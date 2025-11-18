package com.largeblueberry.feature_sheetmusic.domain.repository

import com.largeblueberry.feature_sheetmusic.domain.SheetMusic

interface SheetMusicRepository {
    suspend fun generateSheetMusic(requestBody: Any): Result<SheetMusic>
}

/**
 * 이거는 인터페이스로, 로컬에 저장된 악보 상세 정보를 가져오는 함수를 정의합니다.
 */