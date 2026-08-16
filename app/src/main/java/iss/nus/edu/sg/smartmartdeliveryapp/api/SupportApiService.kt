package iss.nus.edu.sg.smartmartdeliveryapp.api

import iss.nus.edu.sg.smartmartdeliveryapp.model.SupportRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.SupportResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SupportApiService {

    @POST("support/chat")
    suspend fun askQuestion(
        @Body request: SupportRequest
    ): SupportResponse
}