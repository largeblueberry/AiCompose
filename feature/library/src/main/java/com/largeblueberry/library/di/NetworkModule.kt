package com.largeblueberry.library.di

import com.largeblueberry.data.AuthInterceptor
import com.largeblueberry.network.repository.AudioUploadRepository
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
import com.largeblueberry.network.service.NetworkService
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {

        val logger = HttpLoggingInterceptor().apply {
            // Level.BODY: 요청/응답의 헤더와 본문을 모두 보여줍니다. 가장 자세합니다.
            // Level.HEADERS: 헤더 정보만 보여줍니다.
            // Level.BASIC: 요청 라인과 응답 코드만 보여줍니다.
            // Level.NONE: 로그를 남기지 않습니다. (기본값)
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        return OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS) // 연결 타임아웃
            .readTimeout(90, TimeUnit.SECONDS)    // 읽기 타임아웃
            .writeTimeout(90, TimeUnit.SECONDS)   // 쓰기 타임아웃
            .addInterceptor(AuthInterceptor())
            // 2. 생성한 로거를 OkHttpClient에 추가
            // AuthInterceptor 다음에 추가하면, 인증 헤더가 포함된 후의 최종 요청을 볼 수 있어 더 좋습니다.
            .addInterceptor(logger)
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