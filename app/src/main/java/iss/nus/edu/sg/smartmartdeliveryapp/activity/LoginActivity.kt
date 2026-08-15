package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.api.RetrofitClient
import iss.nus.edu.sg.smartmartdeliveryapp.databinding.ActivityLoginBinding
import iss.nus.edu.sg.smartmartdeliveryapp.model.LoginRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.LoginResponse
import iss.nus.edu.sg.smartmartdeliveryapp.utils.TokenManager

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {

        val email = findViewById<EditText>(R.id.etEmail)
            .text.toString()
            .trim()

        val password = findViewById<EditText>(R.id.etPassword)
            .text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(
                this,
                "Please enter email and password",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val request = LoginRequest(
            email = email,
            password = password
        )

        RetrofitClient.apiService
            .login(request)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {

                    if (!response.isSuccessful) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid email or password",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val loginResponse = response.body()

                    if (loginResponse == null) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    // Only DELIVERYMAN can enter this mobile app
                    if (!loginResponse.role.equals(
                            "DELIVERYMAN",
                            ignoreCase = true
                        )
                    ) {
                        Toast.makeText(
                            this@LoginActivity,
                            "This account is not a delivery account",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    // This is the logged-in deliveryman's user ID
                    // Save login details
                    Toast.makeText(this@LoginActivity, "Welcome ${loginResponse.username}", Toast.LENGTH_SHORT).show()
                    TokenManager.saveToken(loginResponse.token)
                    TokenManager.saveUserId(loginResponse.userId)

                    val intent = Intent(
                        this@LoginActivity,
                        ListViewActivity::class.java
                    ).apply {
                        putExtra("RECIPENT_FIRST_NAME", loginResponse.username)
                        putExtra("DELIVERY_MAN_ID", loginResponse.userId)
                    }

                    startActivity(intent)
                    finish()
                }

                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Unable to connect to server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}