package com.largeblueberry.aicompose.retrofit

import com.largeblueberry.aicompose.retrofit.data.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NetworkService {
    @Multipart
    @POST("upload")
    suspend fun upload3gpFile(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): retrofit2.Response<UploadResponse>
}
