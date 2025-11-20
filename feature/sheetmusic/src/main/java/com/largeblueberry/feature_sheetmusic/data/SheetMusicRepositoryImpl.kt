package com.largeblueberry.feature_sheetmusic.data

import android.util.Log
import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.domain.repository.SheetMusicRepository
import com.largeblueberry.local.score.ScoreDao
import com.largeblueberry.local.score.ScoreEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.largeblueberry.network.repository.NetworkSheetMusicRepository as NetworkSheetMusicRepository
import javax.inject.Inject

class SheetMusicRepositoryImpl @Inject constructor(
    private val networkRepository: NetworkSheetMusicRepository, // 이건 network 모듈에 있는 거!
    private val scoreDao: ScoreDao
) : SheetMusicRepository {

    override suspend fun generateSheetMusic(requestBody: Any): Result<SheetMusic> {
        return networkRepository.generateSheetMusic(requestBody)
            .mapCatching { sheetMusicDto ->
                val domainModel = sheetMusicDto.toDomainModel()

                // ✅ 네트워크 통신 성공 시, 도메인 모델을 Entity로 변환하여 DB에 저장
                try {
                    scoreDao.insertScore(domainModel.toEntity())
                    Log.d("RepoImpl", "✅ 생성된 악보 DB 저장 성공: ${domainModel.id}")
                } catch (e: Exception) {
                    Log.e("RepoImpl", "🔴 생성된 악보 DB 저장 실패", e)
                    // 저장 실패가 전체 로직을 중단시킬 필요는 없으므로 에러만 로그로 남깁니다.
                }

                domainModel // 도메인 모델을 반환
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
                // List<ScoreEntity>를 List<SheetMusic>으로 변환
                entityList.map { entity ->
                    entity.toDomainModel()
                }
            }
    }
}

// ✅ SheetMusic(도메인 모델) -> ScoreEntity(DB 모델) 변환 함수
private fun SheetMusic.toEntity(): ScoreEntity {
    return ScoreEntity(
        id = this.id,
        title = this.title,
        scoreUrl = this.scoreUrl ?: "", // scoreUrl은 null이 될 수 없으므로 기본값 처리
        midiUrl = this.midiUrl,
        createdAt = System.currentTimeMillis() // 저장되는 시점의 시간으로 기록
    )
}

// ✅ ScoreEntity(DB 모델) -> SheetMusic(도메인 모델) 변환 함수
// 이 함수는 RepositoryImpl 파일 하단이나 별도의 Mapper 파일에 둘 수 있습니다.
private fun ScoreEntity.toDomainModel(): SheetMusic {
    return SheetMusic(
        id = this.id,
        title = this.title ?: "제목 없음",
        composer = "Unknown", // DB에 없으므로 기본값 설정
        scoreUrl = this.scoreUrl,
        midiUrl = this.midiUrl, // ✅ midiUrl 매핑 추가
        createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(this.createdAt)),
        duration = null,
        key = null,
        tempo = null
    )
}