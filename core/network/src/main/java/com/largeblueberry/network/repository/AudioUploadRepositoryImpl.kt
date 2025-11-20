package com.largeblueberry.network.repository

import com.largeblueberry.network.model.response.UploadResponse
import com.largeblueberry.network.service.NetworkService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AudioUploadRepositoryImpl(private val networkService: NetworkService) : AudioUploadRepository {
    // 인터페이스에 맞춰 반환 타입을 Result<UploadResponse>로 변경
    override suspend fun uploadAudioFile(filePath: String): Result<UploadResponse> {
        val file = File(filePath)
        if (!file.exists()) {
            return Result.failure(Exception("파일이 존재하지 않습니다."))
        }

        val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        return try {
            val response = networkService.upload3gpFile(audioPart)
            if (response.isSuccessful) {
                response.body()?.let { uploadResponse ->
                    // midiUrl이나 scoreUrl이 하나라도 있는지 확인 후 성공 처리
                    if (uploadResponse.midiUrl != null || uploadResponse.scoreUrl != null) {
                        // String(url) 대신 uploadResponse 객체 전체를 반환
                        Result.success(uploadResponse)
                    } else {
                        Result.failure(Exception("서버 응답에 MIDI 또는 악보 URL이 없습니다."))
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