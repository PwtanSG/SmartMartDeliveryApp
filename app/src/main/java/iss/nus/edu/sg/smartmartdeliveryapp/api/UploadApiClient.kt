package iss.nus.edu.sg.smartmartdeliveryapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UploadApiClient {

    private const val BASE_URL =
        "https://mwpd6bfwfd.execute-api.us-east-1.amazonaws.com/"

    val uploadApi: UploadApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(UploadApiService::class.java)
    }
}