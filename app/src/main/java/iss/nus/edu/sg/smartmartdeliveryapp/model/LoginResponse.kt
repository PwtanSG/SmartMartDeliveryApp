package iss.nus.edu.sg.smartmartdeliveryapp.model

data class LoginResponse(
    val token: String,
    val userId: Long,
    val username: String,
    val email: String,
    val role: String
)
