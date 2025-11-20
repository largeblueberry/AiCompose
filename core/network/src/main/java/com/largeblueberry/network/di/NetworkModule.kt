package com.largeblueberry.network.di

import com.largeblueberry.network.interceptor.AuthInterceptor
import com.largeblueberry.network.repository.AudioUploadRepository
import com.largeblueberry.network.repository.AudioUploadRepositoryImpl
import com.largeblueberry.network.repository.NetworkSheetMusicRepository
import com.largeblueberry.network.repository.NetworkSheetMusicRepositoryImpl
import com.largeblueberry.network.service.NetworkService
import com.largeblueberry.network.service.SheetMusicApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        @Named("base_url") baseUrl: String  // 의존성 주입으로 BASE_URL 받기
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)  // 주입받은 baseUrl 사용
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Services
    @Provides
    @Singleton
    fun provideNetworkService(retrofit: Retrofit): NetworkService {
        return retrofit.create(NetworkService::class.java)
    }

    @Provides
    @Singleton
    fun provideSheetMusicApiService(retrofit: Retrofit): SheetMusicApiService {
        return retrofit.create(SheetMusicApiService::class.java)
    }

    // Repositories
    @Provides
    fun provideAudioUploadRepository(service: NetworkService): AudioUploadRepository {
        return AudioUploadRepositoryImpl(service)
    }

    @Provides
    fun provideSheetMusicRepository(service: SheetMusicApiService): NetworkSheetMusicRepository {
        return NetworkSheetMusicRepositoryImpl(service)
    }
}