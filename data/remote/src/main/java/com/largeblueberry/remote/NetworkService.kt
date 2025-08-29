package com.largeblueberry.remote

import com.largeblueberry.remote.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NetworkService {
    @Multipart
    @POST("convert")
    suspend fun upload3gpFile(
        @Part file: MultipartBody.Part,
        // @Part("description") description: RequestBody
    ): Response<UploadResponse>
}

/**
 * @Multipart: 멀티파트 요청을 나타내며, 파일 업로드에 사용됩니다.
 * @POST: HTTP POST 요청을 나타내며, 서버에 데이터를 전송합니다. -> 여기서 convert는 파일 변환입니다.
 * @Part: 멀티파트 요청의 일부로 파일이나 데이터를 포함시킵니다.
 * UploadResponse: 서버로부터의 응답을 나타내는 데이터 클래스입니다. 여기서는 MIDI 파일 URL을 포함합니다.
 */