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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var tts: TextToSpeech? = null
    private lateinit var currentLocale: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeViewModel()
        setupTTS()

        // ✅ בקשה יזומה לקבלת FCM Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "FCM Token: $token")
        }

        // Start monitoring health data
        viewModel.startHealthMonitoring("user_123") // Replace with actual user ID
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
                    "emergency" -> {
                        speakCalmingMessage()
                        launchAlertActivity(response.alertMessage ?: "Emergency situation detected!")
                    }
                    "alert", "stressed" -> {
                        speakCalmingMessage()
                        response.alertMessage?.let { message ->
                            launchAlertActivity(message)
                        }
                    }
                }
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            // Handle error
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
