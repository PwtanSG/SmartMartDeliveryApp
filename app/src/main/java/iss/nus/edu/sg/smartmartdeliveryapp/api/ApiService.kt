package iss.nus.edu.sg.smartmartdeliveryapp.api

import iss.nus.edu.sg.smartmartdeliveryapp.model.LoginRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.LoginResponse
import iss.nus.edu.sg.smartmartdeliveryapp.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface ApiService {

    @POST("api/auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>
}