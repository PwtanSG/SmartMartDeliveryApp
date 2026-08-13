package iss.nus.edu.sg.smartmartdeliveryapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.activity.ListViewActivity

class SmartMartMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "assigned_jobs"
        private const val CHANNEL_NAME = "Assigned jobs"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Next step: send this token to your Spring Boot backend.
        android.util.Log.d("FCM_TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(
            "FCM_MESSAGE",
            "Received: ${message.notification?.body}"
        )
        super.onMessageReceived(message)

        val title =
            message.notification?.title
                ?: message.data["title"]
                ?: "New job assigned"

        val body =
            message.notification?.body
                ?: message.data["body"]
                ?: "A new delivery job has been assigned to you."

        showNotification(title, body)
    }

    private fun showNotification(
        title: String,
        body: String
    ) {
        createNotificationChannel()

        val intent =
            Intent(this, ListViewActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                )
                .setAutoCancel(true)
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setContentIntent(pendingIntent)
                .build()

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description =
                        "Notifications for newly assigned delivery jobs"
                }

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }
}