package com.largeblueberry.feature_sheetmusic.data

import com.largeblueberry.feature_sheetmusic.domain.SheetMusicRepository
import com.largeblueberry.feature_sheetmusic.domain.SheetMusicDetail
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SheetMusicRepositoryImpl @Inject constructor(
    private val localDataSource: SheetMusicLocalDataSource
) : SheetMusicRepository {

    override suspend fun getSheetMusicDetail(id: String): SheetMusicDetail? {
        return try {
            localDataSource.getSheetMusicDetail(id)
        } catch (e: Exception) {
            null // 오류 발생 시 null 반환
        }
    }

    override suspend fun getAllSheetMusic(): List<SheetMusicDetail> {
        return try {
            localDataSource.getAllSheetMusic()
        } catch (e: Exception) {
            emptyList() // 오류 발생 시 빈 리스트 반환
        }
    }

    override fun getAllSheetMusicFlow(): Flow<List<SheetMusicDetail>> {
        return localDataSource.getAllSheetMusicFlow()
    }

    override suspend fun isSheetMusicExists(id: String): Boolean {
        return try {
            localDataSource.isSheetMusicExists(id)
        } catch (e: Exception) {
            false
        }
    }
}
/**
 * 여기서는 로컬에 저장된 악보를 불러오고, 만약에 불러오지 않은 경우 null을 반환합니다.
 * 오로지 여기서는 로컬에 저장되어 있는 모든 것을 가져오고, 추후 ui에서 리사이클러 뷰로 구현합니다.
 */