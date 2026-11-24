package com.largeblueberry.data.repository

import android.util.Log
import com.largeblueberry.data.mapper.ScoreMapper
import com.largeblueberry.data.score.ScoreDao
import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.repository.SheetMusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SheetMusicRepositoryImpl @Inject constructor(
    private val scoreDao: ScoreDao
) : SheetMusicRepository {

    override fun getAllScores(): Flow<List<SheetMusic>> =
        scoreDao.getAllScoresFlow().map { list -> list.map(ScoreMapper::toDomain) }

    override suspend fun getScore(id: String): SheetMusic? =
        scoreDao.getScoreById(id)?.let(ScoreMapper::toDomain)

    override suspend fun readMusicXml(score: SheetMusic): Result<String> = withContext(Dispatchers.IO) {
        runCatching { File(score.musicXmlPath).readText(Charsets.UTF_8) }
    }

    override suspend fun deleteScore(score: SheetMusic): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            scoreDao.deleteScore(ScoreMapper.toEntity(score))
            listOf(score.musicXmlPath, score.midiPath).forEach { path ->
                val f = File(path)
                if (f.exists() && !f.delete()) Log.w(TAG, "could not delete $path")
            }
        }
    }

    private companion object {
        const val TAG = "SheetMusicRepo"
    }
}
