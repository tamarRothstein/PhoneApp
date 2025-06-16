package com.example.myphoneapp.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityMeditationBinding

class MeditationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeditationBinding
    private var isMeditating = false
    private var meditationHandler: Handler? = null
    private var meditationRunnable: Runnable? = null
    private var timeRemaining = 600 // 10 minutes in seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeditationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupUI()
    }

    private fun setupUI() {
        updateTimerDisplay()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnStartMeditation.setOnClickListener {
            if (isMeditating) {
                stopMeditation()
            } else {
                startMeditation()
            }
        }

        binding.btnFinish.setOnClickListener {
            showCompletionDialog()
        }
    }

    private fun startMeditation() {
        isMeditating = true
        binding.btnStartMeditation.text = "Pause"

        startMeditationTimer()
    }

    private fun stopMeditation() {
        isMeditating = false
        binding.btnStartMeditation.text = "Resume"
        meditationHandler?.removeCallbacks(meditationRunnable!!)
    }

    private fun startMeditationTimer() {
        meditationHandler = Handler(Looper.getMainLooper())

        meditationRunnable = Runnable {
            if (!isMeditating) return@Runnable

            timeRemaining--
            updateTimerDisplay()

            if (timeRemaining <= 0) {
                showCompletionDialog()
                return@Runnable
            }

            meditationHandler?.postDelayed(meditationRunnable!!, 1000)
        }

        meditationHandler?.post(meditationRunnable!!)
    }

    private fun updateTimerDisplay() {
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun showCompletionDialog() {
        stopMeditation()

        com.example.myphoneapp.ui.common.CompletionDialog.showMeditationCompletion(
            this,
            onAnotherRound = { resetMeditation() },
            onFinish = { finish() }
        )
    }

    private fun resetMeditation() {
        timeRemaining = 600
        updateTimerDisplay()
        binding.btnStartMeditation.text = "Start Meditation"
    }

    override fun onDestroy() {
        super.onDestroy()
        meditationHandler?.removeCallbacks(meditationRunnable!!)
    }
}