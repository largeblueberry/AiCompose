package com.largeblueberry.data.repository

import android.util.Log
import com.largeblueberry.data.score.ScoreDao
import com.largeblueberry.data.score.ScoreEntity
import com.largeblueberry.domain.model.SheetMusic
import com.largeblueberry.domain.repository.SheetMusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SheetMusicRepositoryImpl @Inject constructor(
    private val scoreDao: ScoreDao
) : SheetMusicRepository {

    override suspend fun generateSheetMusic(requestBody: Any): Result<SheetMusic> {
        return try {
            val dummySheet = SheetMusic(
                id = System.currentTimeMillis().toString(),
                title = "새 악보",
                composer = null,
                scoreUrl = "",
                midiUrl = "",
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                duration = null,
                key = null,
                tempo = null
            )
            scoreDao.insertScore(dummySheet.toEntity())
            Result.success(dummySheet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSheetMusic(sheetMusic: SheetMusic): Result<Unit> {
        return try {
            scoreDao.insertScore(sheetMusic.toEntity())
            Log.d("RepoImpl", "✅ 업로드된 악보 DB 저장 성공: ${sheetMusic.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RepoImpl", "🔴 업로드된 악보 DB 저장 실패", e)
            Result.failure(e)
        }
    }

    override fun getAllScores(): Flow<List<SheetMusic>> {
        return scoreDao.getAllScoresFlow()
            .map { entityList ->
                entityList.map { entity ->
                    entity.toDomainModel()
                }
            }
    }

    override suspend fun deleteScore(score: SheetMusic): Result<Unit> {
        return try {
            scoreDao.deleteScore(score.toEntity())
            Log.d("RepoImpl", "✅ 악보 DB 삭제 성공: ${score.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RepoImpl", "🔴 악보 DB 삭제 실패", e)
            Result.failure(e)
        }
    }
}

private fun SheetMusic.toEntity(): ScoreEntity {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(this.createdAt)
    val createdAtTimestamp = date?.time ?: System.currentTimeMillis()

    return ScoreEntity(
        id = this.id,
        title = this.title,
        scoreUrl = this.scoreUrl,
        midiUrl = this.midiUrl,
        createdAt = createdAtTimestamp,
        composer = this.composer,
        duration = this.duration,
        key = this.key,
        tempo = this.tempo
    )
}

private fun ScoreEntity.toDomainModel(): SheetMusic {
    val createdAtString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(this.createdAt))

    return SheetMusic(
        id = this.id,
        title = this.title,
        scoreUrl = this.scoreUrl,
        midiUrl = this.midiUrl,
        createdAt = createdAtString,
        composer = this.composer,
        duration = this.duration,
        key = this.key,
        tempo = this.tempo
    )
}
