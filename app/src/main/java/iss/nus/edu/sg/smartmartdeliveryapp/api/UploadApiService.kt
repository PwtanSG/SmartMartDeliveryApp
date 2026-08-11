package iss.nus.edu.sg.smartmartdeliveryapp.api

import iss.nus.edu.sg.smartmartdeliveryapp.model.UploadUrlResponse
import iss.nus.edu.sg.smartmartdeliveryapp.model.ViewPhotoRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.ViewPhotoResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface UploadApiService {

    // Call API Gateway
    @POST("delivery-proof/upload-url")
    suspend fun createUploadUrl():
            UploadUrlResponse

    // Upload JPEG directly to the absolute S3 URL
    @Headers("Content-Type: image/jpeg")
    @PUT
    suspend fun uploadPhotoToS3(
        @Url uploadUrl: String,
        @Body imageBody: RequestBody
    ): Response<Unit>

    @POST("delivery-proof/view-url")
    suspend fun getPhotoViewUrl(
        @Body request: ViewPhotoRequest
    ): ViewPhotoResponse
}