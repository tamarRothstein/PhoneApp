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

            val alertMessage = remoteMessage.data["alert_message"] ?: "You received a new alert"
            val heartRate = remoteMessage.data["heartRate"]?.toIntOrNull()
            val stressLevel = remoteMessage.data["stressLevel"]
            val steps = remoteMessage.data["steps"]

            // נשמור הכל ב-SharedPreferences
            val prefs = getSharedPreferences("wellness_prefs", MODE_PRIVATE)
            val editor = prefs.edit()

            editor.putBoolean("should_show_alert", true)
            editor.putString("last_alert_message", alertMessage)

            heartRate?.let { editor.putInt("heartRate", it) }
            stressLevel?.let { editor.putString("stressLevel", it) }
            steps?.let { editor.putString("steps", it) }

            editor.apply()

            // הצגת פופ-אפ מידי
            val intent = Intent(this, AlertActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("ALERT_MESSAGE", alertMessage)
                putExtra("from_firebase_alert", true)
            }

            startActivity(intent)

        } else {
            Log.w("FCM", "Received message with no data payload")
        }
    }

    private fun showNotificationToAlert() {
        val intent = Intent(this, AlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ALERT_MESSAGE", "Hey, what's up? Would you like to try some relaxation together?")
            putExtra("from_firebase_alert", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "wellness_alerts"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // יצירת ערוץ התראות (נדרש מ־Android 8 ומעלה)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Wellness Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_dashboard) // 🎨 שימי לב: ודאי שיש לך אייקון כזה
            .setContentTitle("Relaxation Reminder")
            .setContentText("Tap to take a breath together.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(0, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
    }
}
