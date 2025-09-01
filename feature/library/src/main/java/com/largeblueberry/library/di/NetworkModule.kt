package com.largeblueberry.library.di

import com.largeblueberry.library.dataLayer.repository.AudioUploadRepository
import com.largeblueberry.library.dataLayer.repository.impl.AudioUploadRepositoryImpl
import com.largeblueberry.library.domainLayer.usecase.UploadAudioRecordUseCase
import com.largeblueberry.remote.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.largeblueberry.remote.NetworkService

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃
            .readTimeout(30, TimeUnit.SECONDS)    // 읽기 타임아웃
            .writeTimeout(30, TimeUnit.SECONDS)   // 쓰기 타임아웃
            // .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }) // 로깅 인터셉터 추가 (디버그용)
            .build()
    }

    // Retrofit 인스턴스 제공
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit { // OkHttpClient를 주입받음
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // localproperty에 적용
            .client(okHttpClient) // OkHttpClient 주입
            .addConverterFactory(GsonConverterFactory.create()) // JSON 파싱을 위한 컨버터
            .build()
    }

    // AudioUploadService 인터페이스 구현체 제공
    @Provides
    @Singleton
    fun provideAudioUploadService(retrofit: Retrofit): NetworkService {
        return retrofit.create(NetworkService::class.java)
    }

    @Provides
    fun provideAudioUploadRepository(service: NetworkService): AudioUploadRepository {
        return AudioUploadRepositoryImpl(service)
    }

    @Provides
    fun provideUploadAudioRecordUseCase(repository: AudioUploadRepository): UploadAudioRecordUseCase {
        return UploadAudioRecordUseCase(repository)
    }

}