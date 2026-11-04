package com.largeblueberry.feature_sheetmusic.data

import android.util.Log
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.domain.repository.SheetMusicRepository
import com.largeblueberry.data.score.ScoreDao
import com.largeblueberry.data.score.ScoreEntity
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
            // 도메인 모델을 Entity로 변환하여 DAO에 삭제 요청
            scoreDao.deleteScore(score.toEntity())
            Log.d("RepoImpl", "✅ 악보 DB 삭제 성공: ${score.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RepoImpl", "🔴 악보 DB 삭제 실패", e)
            Result.failure(e)
        }
    }
}

// ⚠️ 수정됨: 도메인 모델의 모든 정보를 Entity로 변환
private fun SheetMusic.toEntity(): ScoreEntity {
    // 날짜 문자열(String)을 타임스탬프(Long)으로 변환
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(this.createdAt)
    val createdAtTimestamp = date?.time ?: System.currentTimeMillis()

    return ScoreEntity(
        id = this.id,
        title = this.title,
        scoreUrl = this.scoreUrl, // ?: "" 불필요하여 제거
        midiUrl = this.midiUrl,
        createdAt = createdAtTimestamp, // 변환된 Long 값 사용

        // ✅ 누락되었던 필드 매핑 추가
        composer = this.composer,
        duration = this.duration,
        key = this.key,
        tempo = this.tempo
    )
}

// ⚠️ 수정됨: Entity의 모든 정보를 도메인 모델로 복원
private fun ScoreEntity.toDomainModel(): SheetMusic {
    // 타임스탬프(Long)를 날짜 문자열(String)으로 변환
    val createdAtString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(this.createdAt))

    return SheetMusic(
        id = this.id,
        title = this.title, // DB의 title이 non-null이라면 ?: "제목 없음" 제거 가능
        scoreUrl = this.scoreUrl,
        midiUrl = this.midiUrl,
        createdAt = createdAtString,

        // ✅ 하드코딩 대신 Entity에서 실제 값 가져오기
        composer = this.composer,
        duration = this.duration,
        key = this.key,
        tempo = this.tempo
    )
}
