package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.api.RetrofitClient
import iss.nus.edu.sg.smartmartdeliveryapp.model.RegisterRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener {
            registerDeliveryMan()
        }

        tvLogin.setOnClickListener {

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            startActivity(intent)
            finish()
        }
    }

    private fun registerDeliveryMan() {

        val username =
            etUsername.text.toString().trim()

        val email =
            etEmail.text.toString().trim()

        val password =
            etPassword.text.toString()

        // Validation
        if (username.isEmpty()) {

            etUsername.error = "Username is required"
            etUsername.requestFocus()
            return
        }

        if (email.isEmpty()) {

            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {

            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {

            etPassword.error =
                "Password must be at least 6 characters"

            etPassword.requestFocus()
            return
        }

        val request = RegisterRequest(
            username = username,
            email = email,
            password = password,
            role = "DELIVERYMAN"
        )

        // Disable button to prevent double click
        btnRegister.isEnabled = false
        btnRegister.text = "Registering..."

        lifecycleScope.launch {

            try {

                val response =
                    RetrofitClient.apiService.register(request)

                if (response.isSuccessful) {

                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Go back to LoginActivity
                    val intent = Intent(
                        this@RegisterActivity,
                        LoginActivity::class.java
                    )

                    // Optional: pre-fill email on login screen
                    intent.putExtra(
                        "REGISTERED_EMAIL",
                        email
                    )

                    startActivity(intent)

                    finish()

                } else {

                    val errorMessage =
                        response.errorBody()
                            ?.string()
                            ?: "Registration failed"

                    Toast.makeText(
                        this@RegisterActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@RegisterActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnRegister.isEnabled = true
                btnRegister.text = "Register"
            }
        }
    }
}