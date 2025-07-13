package com.largeblueberry.aicompose.retrofit

import com.largeblueberry.aicompose.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)  // 연결 시간 제한
        .readTimeout(30, TimeUnit.SECONDS)     // 읽기(응답) 시간 제한
        .writeTimeout(30, TimeUnit.SECONDS)    // 쓰기(요청 전송) 시간 제한
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val audioUploadService: NetworkService = retrofit.create(NetworkService::class.java)
}
