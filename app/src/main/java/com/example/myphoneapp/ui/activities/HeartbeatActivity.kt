package com.example.myphoneapp.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.R
import com.example.myphoneapp.databinding.ActivityHeartbeatBinding
import kotlin.random.Random

class HeartbeatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeartbeatBinding
    private var isListening = false
    private var heartbeatHandler = Handler(Looper.getMainLooper())
    private var heartbeatRunnable: Runnable? = null
    private var sessionStartTime = 0L
    private var currentStep = 0

    private val steps = arrayOf(
        "Find a quiet and comfortable place to sit",
        "Place your finger gently on your wrist pulse",
        "Close your eyes and focus on your heartbeat",
        "Breathe slowly and feel your natural rhythm",
        "Notice how your heart rate changes with breathing",
        "Stay connected to this natural rhythm"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeartbeatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupClickListeners()
        updateInstructions()
    }

    private fun setupViews() {
        binding.statusText.text = "Ready to begin?"
        binding.timerText.text = "Time: 00:00"
        binding.heartIcon.text = "🤍"
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.startButton.setOnClickListener {
            if (!isListening) {
                startHeartbeatSession()
            } else {
                stopHeartbeatSession()
            }
        }

        binding.resetButton.setOnClickListener {
            resetSession()
        }

        binding.nextStepButton.setOnClickListener {
            nextStep()
        }
    }

    private fun startHeartbeatSession() {
        isListening = true
        sessionStartTime = System.currentTimeMillis()

        binding.startButton.text = "Stop Listening"
        binding.statusText.text = "Listening to your heartbeat..."
        binding.resetButton.visibility = android.view.View.VISIBLE

        startHeartbeatAnimation()
        startTimer()
    }

    private fun stopHeartbeatSession() {
        isListening = false
        heartbeatHandler.removeCallbacks(heartbeatRunnable ?: return)

        binding.startButton.text = "Start Listening"
        binding.statusText.text = "Session paused"
        binding.heartIcon.text = "🤍"

        showCompletionDialog()
    }

    private fun resetSession() {
        isListening = false
        heartbeatHandler.removeCallbacks(heartbeatRunnable ?: return)
        currentStep = 0

        binding.startButton.text = "Start Listening"
        binding.statusText.text = "Ready to begin?"
        binding.timerText.text = "Time: 00:00"
        binding.heartIcon.text = "🤍"
        binding.resetButton.visibility = android.view.View.GONE
        binding.nextStepButton.visibility = android.view.View.GONE

        updateInstructions()
    }

    private fun nextStep() {
        currentStep++
        if (currentStep >= steps.size) {
            currentStep = 0
        }
        updateInstructions()
    }

    private fun updateInstructions() {
        binding.stepCounter.text = "Step ${currentStep + 1} of ${steps.size}"
        binding.instructionText.text = steps[currentStep]

        if (currentStep > 0) {
            binding.nextStepButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun startHeartbeatAnimation() {
        // סימולציה של דופק - בעתיד ניתן לחבר לחיישן אמיתי
        val heartRate = Random.nextInt(60, 100) // BPM רנדומלי
        val interval = (60000L / heartRate) // מילישניות בין פעימות

        heartbeatRunnable = object : Runnable {
            override fun run() {
                if (isListening) {
                    // אנימציה של פעימת לב
                    binding.heartIcon.text = "❤️"
                    binding.heartIcon.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(200)
                        .withEndAction {
                            binding.heartIcon.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .withEndAction {
                                    binding.heartIcon.text = "🤍"
                                }
                                .start()
                        }
                        .start()

                    heartbeatHandler.postDelayed(this, interval)
                }
            }
        }
        heartbeatHandler.post(heartbeatRunnable!!)
    }

    private fun startTimer() {
        val timerRunnable = object : Runnable {
            override fun run() {
                if (isListening) {
                    val elapsed = System.currentTimeMillis() - sessionStartTime
                    val seconds = (elapsed / 1000) % 60
                    val minutes = (elapsed / 1000) / 60
                    binding.timerText.text = String.format("Time: %02d:%02d", minutes, seconds)

                    heartbeatHandler.postDelayed(this, 1000)
                }
            }
        }
        heartbeatHandler.post(timerRunnable)
    }

    private fun showCompletionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Session Complete")
            .setMessage("Well done! You've successfully connected with your natural heartbeat rhythm. This practice helps reduce stress and increase body awareness.")
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Start Again") { dialog, _ ->
                dialog.dismiss()
                resetSession()
            }
            .create()

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        heartbeatHandler.removeCallbacks(heartbeatRunnable ?: return)
    }
}