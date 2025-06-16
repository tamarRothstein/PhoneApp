package com.example.myphoneapp.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityVoiceGuidanceBinding

class VoiceGuidanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceGuidanceBinding
    private var isGuidanceActive = false
    private var guidanceHandler: Handler? = null
    private var guidanceRunnable: Runnable? = null
    private var currentStep = 0

    private val guidanceSteps = listOf(
        "I'm here with you right now. You're not alone in this moment.",
        "Take a deep breath with me... In through your nose... and slowly out through your mouth.",
        "You're doing great. Let's take another breath together... breathe in... and slowly release.",
        "Feel your body relaxing. Notice how your shoulders drop and your muscles soften.",
        "You are safe. You are strong. This feeling will pass, and you will be okay.",
        "Let's take one more deep breath together... in... and out. You've done wonderfully."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceGuidanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupUI()
    }

    private fun setupUI() {
        binding.voiceText.text = guidanceSteps[0]
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            if (isGuidanceActive) {
                nextStep()
            } else {
                continueGuidance()
            }
        }

        binding.btnPause.setOnClickListener {
            if (isGuidanceActive) {
                pauseGuidance()
            } else {
                showCompletionDialog()
            }
        }
    }

    private fun continueGuidance() {
        isGuidanceActive = true
        binding.btnContinue.text = "Next"
        binding.btnPause.text = "Pause"
    }

    private fun pauseGuidance() {
        isGuidanceActive = false
        binding.btnContinue.text = "Continue"
        binding.btnPause.text = "Finish"
        guidanceHandler?.removeCallbacks(guidanceRunnable!!)
    }

    private fun nextStep() {
        currentStep++

        if (currentStep >= guidanceSteps.size) {
            showCompletionDialog()
            return
        }

        // Update text
        binding.voiceText.text = guidanceSteps[currentStep]
    }

    private fun showCompletionDialog() {
        pauseGuidance()

        com.example.myphoneapp.ui.common.CompletionDialog.showVoiceGuidanceCompletion(
            this,
            onAnotherRound = { resetGuidance() },
            onFinish = { finish() }
        )
    }

    private fun resetGuidance() {
        currentStep = 0
        isGuidanceActive = false
        binding.voiceText.text = guidanceSteps[0]
        binding.btnContinue.text = "Continue"
        binding.btnPause.text = "Finish"
    }

    override fun onDestroy() {
        super.onDestroy()
        guidanceHandler?.removeCallbacks(guidanceRunnable!!)
    }
}