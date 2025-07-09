package com.largeblueberry.aicompose.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://teamproject.p-e.kr" // 서버 URL로 변경

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)  // 연결 시간 제한
        .readTimeout(30, TimeUnit.SECONDS)     // 읽기(응답) 시간 제한
        .writeTimeout(30, TimeUnit.SECONDS)    // 쓰기(요청 전송) 시간 제한
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val audioUploadService: NetworkService = retrofit.create(NetworkService::class.java)
}
