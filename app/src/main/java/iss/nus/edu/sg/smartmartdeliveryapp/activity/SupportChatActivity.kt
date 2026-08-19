package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.api.SupportRetrofitClient
import iss.nus.edu.sg.smartmartdeliveryapp.model.SupportRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.widget.ImageButton

class SupportChatActivity :
    AppCompatActivity() {

    private lateinit var etQuestion: EditText
    private lateinit var tvConversation: TextView
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var chatScrollView: ScrollView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_support_chat
        )
        findViewById<ImageButton>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        etQuestion =
            findViewById(R.id.etQuestion)

        tvConversation =
            findViewById(R.id.tvConversation)

        btnSend =
            findViewById(R.id.btnSend)

        progressBar =
            findViewById(R.id.progressBar)

        chatScrollView =
            findViewById(R.id.chatScrollView)

        btnSend.setOnClickListener {
            val question =
                etQuestion.text
                    .toString()
                    .trim()

            if (question.isBlank()) {
                etQuestion.error =
                    "Enter a question"

                return@setOnClickListener
            }

            askSupport(question)
        }
    }

    private fun askSupport(
        question: String
    ) {
        showLoading(true)

        appendMessage(
            "\n\nYou: $question"
        )

        etQuestion.setText("")

        lifecycleScope.launch {
            try {
                val response =
                    SupportRetrofitClient.api
                        .askQuestion(
                            SupportRequest(
                                question = question
                            )
                        )

                appendMessage(
                    "\n\nSupport: ${response.answer}"
                )

            } catch (e: HttpException) {
                val errorBody =
                    e.response()
                        ?.errorBody()
                        ?.string()

                Log.e(
                    "SUPPORT_API",
                    "HTTP ${e.code()}: $errorBody",
                    e
                )

                appendMessage(
                    "\n\nSupport: Unable to answer. " +
                            "HTTP ${e.code()}"
                )

            } catch (e: IOException) {
                Log.e(
                    "SUPPORT_API",
                    "Connection failed",
                    e
                )

                appendMessage(
                    "\n\nSupport: Check your internet connection."
                )

            } catch (e: Exception) {
                Log.e(
                    "SUPPORT_API",
                    "Unexpected error",
                    e
                )

                appendMessage(
                    "\n\nSupport: Something went wrong."
                )

            } finally {
                showLoading(false)
            }
        }
    }

    private fun appendMessage(
        message: String
    ) {
        tvConversation.append(message)

        chatScrollView.post {
            chatScrollView.fullScroll(
                View.FOCUS_DOWN
            )
        }
    }

    private fun showLoading(
        loading: Boolean
    ) {
        progressBar.visibility =
            if (loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnSend.isEnabled = !loading
    }
}