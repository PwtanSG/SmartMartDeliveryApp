package iss.nus.edu.sg.smartmartdeliveryapp.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupportRetrofitClient {

    private const val BASE_URL =
        "https://p53m5brbok.execute-api.us-east-1.amazonaws.com/"

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                HttpLoggingInterceptor.Level.BODY
        }

    private val okHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                loggingInterceptor
            )
            .build()

    val api: SupportApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(
                SupportApiService::class.java
            )
    }
}