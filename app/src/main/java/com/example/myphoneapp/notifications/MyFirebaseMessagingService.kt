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

        // בדיקה אם יש data בלבד
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Data payload: ${remoteMessage.data}")

            val alertMessage = remoteMessage.data["alert_message"] ?: "You received a new alert"
            val intent = Intent(this, AlertActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("ALERT_MESSAGE", alertMessage)
                putExtra("from_firebase_alert", true)
            }

            // הצגת פופ-אפ (לא notification bar)
            startActivity(intent)

            // סימון ב־SharedPreferences למקרה של פתיחה מה-Launcher
            val prefs = getSharedPreferences("wellness_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("should_show_alert", true).apply()

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
