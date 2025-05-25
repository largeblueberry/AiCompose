package com.largeblueberry.aicompose.retrofit

import com.largeblueberry.aicompose.retrofit.data.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NetworkService {
    @Multipart
    @POST("upload") // 실제 엔드포인트에 맞게 수정 -> 서버 주소 받기
    fun upload3gpFile(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<UploadResponse>
}