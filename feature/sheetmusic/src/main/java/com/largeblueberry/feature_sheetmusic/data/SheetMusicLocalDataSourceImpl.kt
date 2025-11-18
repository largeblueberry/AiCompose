package com.largeblueberry.feature_sheetmusic.data

import com.largeblueberry.local.score.ScoreDao
import com.largeblueberry.feature_sheetmusic.domain.SheetMusicDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SheetMusicLocalDataSourceImpl @Inject constructor(
    private val scoreDao: ScoreDao  // AudioDatabase에서 가져온 ScoreDao 사용
) : SheetMusicLocalDataSource {

    override suspend fun getSheetMusicDetail(id: String): SheetMusicDetail? {
        return scoreDao.getScoreById(id)?.toDomain()
    }

    override suspend fun getAllSheetMusic(): List<SheetMusicDetail> {
        return scoreDao.getAllScores().toDomain()
    }

    override fun getAllSheetMusicFlow(): Flow<List<SheetMusicDetail>> {
        return scoreDao.getAllScoresFlow().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun saveSheetMusicDetail(sheetMusic: SheetMusicDetail) {
        scoreDao.insertScore(sheetMusic.toEntity())
    }

    override suspend fun deleteSheetMusicDetail(id: String) {
        scoreDao.deleteScoreById(id)
    }

    override suspend fun isSheetMusicExists(id: String): Boolean {
        return scoreDao.isScoreExists(id)
    }
}