package com.largeblueberry.aicompose.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "YOUR_SERVER_URL" // 서버 URL로 변경

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val audioUploadService: NetworkService = retrofit.create(NetworkService::class.java)
}
