package com.example.myphoneapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myphoneapp.R
import com.example.myphoneapp.ui.alert.AlertActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Data payload: ${remoteMessage.data}")

            val prefs = getSharedPreferences("wellness_prefs", MODE_PRIVATE)
            val editor = prefs.edit()

            val heartRate = remoteMessage.data["heart_rate"]?.toIntOrNull() ?: -1
            val stressLevel = remoteMessage.data["stress_level"]
            val rageScore = remoteMessage.data["rage_score"]?.toFloatOrNull()
            val hrv = remoteMessage.data["hrv"]?.toFloatOrNull()
            val deviceId = remoteMessage.data["device_id"]
            val timestamp = remoteMessage.data["timestamp"]?.toLongOrNull()
            val alert = remoteMessage.data["alert"]

            Log.d("FCM_DEBUG", "Going to save: heartRate=$heartRate, stressLevel=$stressLevel, rageScore=$rageScore, hrv=$hrv, deviceId=$deviceId, timestamp=$timestamp")

            editor.putBoolean("should_show_alert", false)
            if (heartRate >= 0) editor.putInt("heartRate", heartRate)
            stressLevel?.let { editor.putString("stressLevel", it) }
            rageScore?.let { editor.putFloat("rageScore", it) }
            hrv?.let { editor.putFloat("hrv", it) }
            deviceId?.let { editor.putString("deviceId", it) }
            timestamp?.let { editor.putLong("timestamp", it) }
            editor.apply()

            // אם יש מפתח "alert" ב-Firebase - נציג נוטיפיקציה עם הודעה קבועה
            if (!alert.isNullOrEmpty()) {
                showNotification()
            }
        } else {
            Log.w("FCM", "Received message with no data payload")
        }
    }

    private fun showNotification() {
        val channelId = "default_channel_id"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val title = "Relaxation Alert"
        val message = "Take a moment to breathe and calm yourself."

        val intent = Intent(this, AlertActivity::class.java).apply {
            putExtra("ALERT_MESSAGE", message)
            putExtra("from_firebase_alert", true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
    }
}