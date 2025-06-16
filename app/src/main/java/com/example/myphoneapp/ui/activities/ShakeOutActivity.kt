package com.example.myphoneapp.ui.activities

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityShakeOutBinding

class ShakeOutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShakeOutBinding
    private var isShaking = false
    private var shakeHandler: Handler? = null
    private var shakeRunnable: Runnable? = null
    private var currentStep = 0
    private var timeRemaining = 60 // 1 minute in seconds

    private val shakeSteps = listOf(
        "Shake your hands gently for 10 seconds",
        "Now shake your arms and shoulders",
        "Shake your whole upper body",
        "Gently shake your legs",
        "Shake your whole body softly",
        "Take a deep breath and relax"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShakeOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupUI()
    }

    private fun setupUI() {
        binding.shakeInstruction.text = "Ready to shake out the tension?"
        binding.timerText.text = "1:00"
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnStartShake.setOnClickListener {
            if (isShaking) {
                stopShaking()
            } else {
                startShaking()
            }
        }

        binding.btnFinish.setOnClickListener {
            showCompletionDialog()
        }
    }

    private fun startShaking() {
        isShaking = true
        binding.btnStartShake.text = "Stop"
        currentStep = 0
        timeRemaining = 60

        startShakeSequence()
        startTimer()
    }

    private fun stopShaking() {
        isShaking = false
        binding.btnStartShake.text = "Start Shaking"
        shakeHandler?.removeCallbacks(shakeRunnable!!)

        binding.shakeInstruction.text = "Shaking paused. Tap 'Start' to continue"
    }

    private fun startShakeSequence() {
        if (!isShaking || currentStep >= shakeSteps.size) {
            if (currentStep >= shakeSteps.size) {
                showCompletionDialog()
            }
            return
        }

        binding.shakeInstruction.text = shakeSteps[currentStep]

        // Animate the instruction text
        val animator = ObjectAnimator.ofFloat(binding.shakeInstruction, "alpha", 0f, 1f)
        animator.duration = 500
        animator.start()

        shakeHandler = Handler(Looper.getMainLooper())
        shakeRunnable = Runnable {
            if (!isShaking) return@Runnable

            currentStep++
            shakeHandler?.postDelayed({
                startShakeSequence()
            }, 500)
        }

        shakeHandler?.postDelayed(shakeRunnable!!, 10000) // 10 seconds per step
    }

    private fun startTimer() {
        val timerHandler = Handler(Looper.getMainLooper())

        val timerRunnable = object : Runnable {
            override fun run() {
                if (!isShaking || timeRemaining <= 0) {
                    if (timeRemaining <= 0) {
                        showCompletionDialog()
                    }
                    return
                }

                timeRemaining--
                val minutes = timeRemaining / 60
                val seconds = timeRemaining % 60
                binding.timerText.text = String.format("%d:%02d", minutes, seconds)

                timerHandler.postDelayed(this, 1000)
            }
        }

        timerHandler.post(timerRunnable)
    }

    private fun showCompletionDialog() {
        stopShaking()

        com.example.myphoneapp.ui.common.CompletionDialog(
            this,
            "Well Done! 💪",
            "You released the tension beautifully",
            "🌟",
            onAnotherRound = { resetShaking() },
            onFinish = { finish() }
        ).show()
    }

    private fun resetShaking() {
        currentStep = 0
        timeRemaining = 60
        binding.shakeInstruction.text = "Ready to shake out the tension?"
        binding.timerText.text = "1:00"
        binding.btnStartShake.text = "Start Shaking"
    }

    override fun onDestroy() {
        super.onDestroy()
        shakeHandler?.removeCallbacks(shakeRunnable!!)
    }
}