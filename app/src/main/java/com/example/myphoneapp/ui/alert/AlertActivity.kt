package com.example.myphoneapp.ui.alert

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityAlertBinding
import com.example.myphoneapp.MainActivity
import kotlin.system.exitProcess

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding
    private var isThankYouState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get alert message from intent
        val alertMessage = intent.getStringExtra("ALERT_MESSAGE")
            ?: "Hey, what's up? Would you like to try some relaxation together?"

        setupUI(alertMessage)
        setupClickListeners()
    }

    private fun setupUI(message: String) {
        binding.alertMessage.text = message
    }

    private fun setupClickListeners() {
        binding.btnYes.setOnClickListener {
            if (isThankYouState) {
                finish()
            } else {
                // ✅ כן - עבור ישר ל-Wellness
                Log.d("ALERT_ACTIVITY", "User clicked Yes - navigating to Wellness")

                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("navigate_to", "wellness")
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
        }

        binding.btnNo.setOnClickListener {
            Log.d("ALERT_ACTIVITY", "User clicked No - closing alert")
            finish()
        }

        binding.btnNo.setOnClickListener {
            if (isThankYouState) {
                // ✅ סגירת האפליקציה לגמרי
                finishAffinity()
                exitProcess(0)
            } else {
                showThankYouMessage()
            }
        }
    }

    private fun showThankYouMessage() {
        isThankYouState = true
        binding.alertMessage.text = "Okay, no problem! Have a great day! 😊\n\nRemember, I'm here whenever you need support."
        binding.btnYes.text = "Thanks"
        binding.btnNo.text = "Close App"
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}