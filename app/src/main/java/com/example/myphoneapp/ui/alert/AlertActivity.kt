package com.example.myphoneapp.ui.alert

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityAlertBinding
import com.example.myphoneapp.ui.alert.WellnessOptionsActivity

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding
    private var isThankYouState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get alert message from intent
        val alertMessage = intent.getStringExtra("ALERT_MESSAGE") ?:
        "Hey, what's up? Would you like to try some relaxation together?"

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
                // Navigate to wellness options activity
                startActivity(Intent(this, WellnessOptionsActivity::class.java))
                finish()
            }
        }

        binding.btnNo.setOnClickListener {
            if (isThankYouState) {
                finish()
            } else {
                // Show thank you message and update state
                showThankYouMessage()
            }
        }
    }

    private fun showThankYouMessage() {
        isThankYouState = true
        binding.alertMessage.text = "Okay, no problem! Have a great day! 😊\n\nRemember, I'm here whenever you need support."
        binding.btnYes.text = "Thanks"
        binding.btnNo.text = "Close"
    }

    override fun onBackPressed() {
        // Allow back button to close the activity
        super.onBackPressed()
    }
}