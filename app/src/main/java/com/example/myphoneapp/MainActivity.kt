package com.example.myphoneapp

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myphoneapp.databinding.ActivityMainBinding
import com.example.myphoneapp.ui.main.MainViewModel
import com.example.myphoneapp.ui.alert.AlertActivity
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.myphoneapp.ui.dashboard.DashboardFragment
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var tts: TextToSpeech? = null
    private lateinit var currentLocale: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // בדיקה של פרמטרים מיוחדים
        val extras = intent.extras
        val alertMessage = extras?.getString("ALERT_MESSAGE")
        val hasAlert = extras?.containsKey("ALERT_MESSAGE") == true
        val navigateToDashboard = intent.getBooleanExtra("navigate_to_dashboard", false)
        val fromNotification = intent.getBooleanExtra("from_notification", false)
        val fromFirebaseAlert = intent.getBooleanExtra("from_firebase_alert", false)

        Log.d("CHECK_INTENT", "ALERT_MESSAGE = $alertMessage | hasAlert=$hasAlert | navigateToDashboard=$navigateToDashboard | fromFirebaseAlert=$fromFirebaseAlert")

        // ✅ אם זה פוש מ־Firebase עם alert – נפתח ישר את AlertActivity
        if (hasAlert && !alertMessage.isNullOrBlank() && alertMessage != "null" && fromFirebaseAlert && intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0) {
            val alertIntent = Intent(this, AlertActivity::class.java)
            alertIntent.putExtra("ALERT_MESSAGE", alertMessage)
            alertIntent.putExtra("from_firebase_alert", true)
            alertIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(alertIntent)
            finish()
            return
        }

        // ✅ אם יש דגל בשמירה מקומית (SharedPreferences)
        val prefs = getSharedPreferences("wellness_prefs", MODE_PRIVATE)
        val shouldShowAlertFromFirebase = prefs.getBoolean("should_show_alert", false)

        if (shouldShowAlertFromFirebase && !hasAlert && !navigateToDashboard && !fromNotification) {
            Log.d("CHECK_INTENT", "🏏 Detected notification click via SharedPreferences - opening AlertActivity")

            // נקה את הדגל
            prefs.edit().putBoolean("should_show_alert", false).apply()

            val alertIntent = Intent(this, AlertActivity::class.java)
            alertIntent.putExtra("ALERT_MESSAGE", "Hey, what's up? Would you like to try some relaxation together?")
            alertIntent.putExtra("from_firebase_alert", true)
            alertIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(alertIntent)
            finish()
            return
        }

        // ✅ אם לא – המשך רגיל לדשבורד
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeViewModel()
        setupTTS()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "FCM Token: $token")
        }

        // ✅ אם יש בקשה לעבור ל־Wellness Fragment
        val target = intent.getStringExtra("navigate_to")
        if (target == "wellness") {
            binding.root.post {
                val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNavigation.selectedItemId = R.id.wellnessFragment
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupTTS() {
        currentLocale = when (Locale.getDefault().language) {
            "he" -> Locale("he", "IL")
            "ru" -> Locale("ru", "RU")
            else -> Locale.US
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(currentLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "שפה לא נתמכת במכשיר", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun speakCalmingMessage() {
        val message = when (currentLocale.language) {
            "he" -> "היי, הכל בסדר. תנשמי עמוק. את לא לבד."
            "ru" -> "Привет, всё хорошо. Ты не одна. Дыши глубоко."
            else -> "Hey, everything is okay. You're not alone. Breathe deeply."
        }
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun observeViewModel() {
        viewModel.serverResponse.observe(this) { response ->
            if (response.shouldAlert) {
                when (response.emotionalState.state) {
                    "emergency", "alert", "stressed" -> {
                        speakCalmingMessage()
                        val msg = response.alertMessage ?: "Take a deep breath!"
                        // השבתנו זמנית כי זה גרם לבעיה
                        // launchAlertActivity(msg)
                    }
                }
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            // Handle error if needed
        }
    }

    private fun launchAlertActivity(message: String) {
        val intent = Intent(this, AlertActivity::class.java)
        intent.putExtra("ALERT_MESSAGE", message)
        startActivity(intent)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
