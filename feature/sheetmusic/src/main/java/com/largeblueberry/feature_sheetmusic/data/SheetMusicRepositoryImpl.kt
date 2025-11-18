package com.largeblueberry.feature_sheetmusic.data

import com.largeblueberry.feature_sheetmusic.domain.SheetMusic
import com.largeblueberry.feature_sheetmusic.domain.repository.SheetMusicRepository
import com.largeblueberry.network.repository.NetworkSheetMusicRepository as NetworkSheetMusicRepository
import javax.inject.Inject

class SheetMusicRepositoryImpl @Inject constructor(
    private val networkRepository: NetworkSheetMusicRepository // 이건 network 모듈에 있는 거!
) : SheetMusicRepository {

    override suspend fun generateSheetMusic(requestBody: Any): Result<SheetMusic> {
        return networkRepository.generateSheetMusic(requestBody)
            .mapCatching { sheetMusicDto ->
                sheetMusicDto.toDomainModel()
            }
    }
}