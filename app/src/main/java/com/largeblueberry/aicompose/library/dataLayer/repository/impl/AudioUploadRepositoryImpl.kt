package com.largeblueberry.aicompose.library.dataLayer.repository.impl

import com.largeblueberry.aicompose.library.dataLayer.repository.AudioUploadRepository
import com.largeblueberry.aicompose.retrofit.NetworkService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AudioUploadRepositoryImpl(private val networkService: NetworkService) : AudioUploadRepository{
    override suspend fun uploadAudioFile(filePath: String): Result<String> {
        val file = File(filePath)
        if (!file.exists()) {
            return Result.failure(Exception("파일이 존재하지 않습니다."))
        }
        val requestFile = RequestBody.create("audio/*".toMediaTypeOrNull(), file)
        val audioPart = MultipartBody.Part.createFormData("file", file.name, requestFile)
        return try {
            val response = networkService.upload3gpFile(audioPart)
            if (response.isSuccessful) {
                response.body()?.let { result ->
                    if (result.midiUrl != null) {
                        Result.success(result.midiUrl)
                    } else {
                        Result.failure(Exception("서버 응답에 MIDI URL이 없습니다."))
                    }
                } ?: Result.failure(Exception("서버 응답이 비어있습니다."))
            } else {
                Result.failure(Exception("서버 오류: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}