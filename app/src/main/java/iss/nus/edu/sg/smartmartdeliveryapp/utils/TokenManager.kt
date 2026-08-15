package iss.nus.edu.sg.smartmartdeliveryapp.utils

object TokenManager {

    private var token: String? = null
    private var userId: Long? = null

    fun saveToken(newToken: String) {
        token = newToken
    }

    fun getToken(): String? {
        return token
    }

    fun saveUserId(newUserId: Long) {
        userId = newUserId
    }

    fun getUserId(): Long? {
        return userId
    }

    fun clear() {
        token = null
        userId = null
    }
}