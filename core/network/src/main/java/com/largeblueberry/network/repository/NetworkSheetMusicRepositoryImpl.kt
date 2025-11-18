package com.largeblueberry.network.repository

import com.largeblueberry.network.model.response.SheetMusicDto
import com.largeblueberry.network.service.SheetMusicApiService

class NetworkSheetMusicRepositoryImpl(
    private val sheetMusicApiService: SheetMusicApiService
) : NetworkSheetMusicRepository {

    override suspend fun generateSheetMusic(requestBody: Any): Result<SheetMusicDto> {
        return try {
            val response = sheetMusicApiService.generateScore(requestBody)
            if (response.isSuccessful) {
                response.body()?.let { sheetMusicDto ->
                    Result.success(sheetMusicDto)
                } ?: Result.failure(Exception("서버 응답이 비어있습니다."))
            } else {
                Result.failure(Exception("서버 오류: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}