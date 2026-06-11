package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.random.Random


class FCMService : FirebaseMessagingService() {


    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.e("TEST_FCM", "ТОКЕН: $token")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            val push = gson.fromJson(message.data[content], PushWithId::class.java)
            val myID = AppAuth.getInstance().authState.value.id
            when {
                push.recipientId == null -> handlePush(push.content)

                push.recipientId == myID -> handlePush(push.content)

                push.recipientId == 0L && push.recipientId != myID -> AppAuth.getInstance().sendPushToken()

                push.recipientId != 0L && push.recipientId != myID -> AppAuth.getInstance().sendPushToken()

            }
        } catch (e: Exception) {
            Log.e("FCM Service", "Error notification")
        }

    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken()
    }

    fun notify(notification: Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }

    fun handlePush(content: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notify(notification)
    }
}

data class PushWithId(
    val recipientId: Long?,
    val content: String
)