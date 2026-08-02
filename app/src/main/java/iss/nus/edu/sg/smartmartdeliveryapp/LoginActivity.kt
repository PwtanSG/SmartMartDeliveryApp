package iss.nus.edu.sg.smartmartdeliveryapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.smartmartdeliveryapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username =
                binding.etUsername.text.toString().trim()

            val password =
                binding.etPassword.text.toString()

            if (username.isEmpty()) {
                binding.etUsername.error =
                    "Username is required"
                binding.etUsername.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etPassword.error =
                    "Password is required"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            login(username, password)
        }
    }

    private fun login(
        username: String,
        password: String
    ) {
        Toast.makeText(
            this,
            "Username: $username",
            Toast.LENGTH_SHORT
        ).show()

        // Call Spring Boot using Retrofit here.
    }
}