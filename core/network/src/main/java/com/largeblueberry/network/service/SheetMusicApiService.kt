package com.largeblueberry.network.service

import com.largeblueberry.network.model.response.SheetMusicDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 악보 관련 API 서비스 인터페이스
 */
interface SheetMusicApiService {

    /**
     * 새로운 악보 URL을 요청
     * POST 요청으로 허밍 데이터를 보내고 악보 URL을 받음
     */
    @POST("api/score")
    suspend fun generateScore(@Body requestBody: Any): Response<SheetMusicDto>

    /**
     * 특정 악보 URL 조회 (필요시)
     */
    @GET("api/score/{id}")
    suspend fun getScore(@Path("id") scoreId: String): Response<SheetMusicDto>
}