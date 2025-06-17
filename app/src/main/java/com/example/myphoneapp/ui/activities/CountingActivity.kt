package com.example.myphoneapp.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityCountingBinding

class CountingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountingBinding
    private var currentNumber = 0
    private var isActive = false
    private var handler = Handler(Looper.getMainLooper())
    private var countingRunnable: Runnable? = null
    private val maxCount = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        binding.numberDisplay.text = "0"
        binding.instructionText.text = "Ready to Start Counting"
        binding.startButton.text = "Start Counting"
        binding.stopButton.isEnabled = false
        binding.stopButton.alpha = 0.5f
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.startButton.setOnClickListener {
            if (!isActive) {
                startCounting()
            } else {
                resetCounting()
            }
        }

        binding.stopButton.setOnClickListener {
            stopCounting()
        }

        binding.doneButton.setOnClickListener {
            finish()
        }
    }

    private fun startCounting() {
        isActive = true
        currentNumber = 0

        binding.startButton.text = "Reset"
        binding.instructionText.text = "Focus on each number as it appears"
        binding.stopButton.isEnabled = true
        binding.stopButton.alpha = 1f

        startCountingAnimation()
    }

    private fun stopCounting() {
        isActive = false
        handler.removeCallbacks(countingRunnable ?: return)

        binding.instructionText.text = "Counting paused at $currentNumber"
        binding.startButton.text = "Start Counting"
        binding.stopButton.isEnabled = false
        binding.stopButton.alpha = 0.5f

        if (currentNumber >= maxCount) {
            showCompletionDialog()
        }
    }

    private fun resetCounting() {
        isActive = false
        handler.removeCallbacks(countingRunnable ?: return)
        currentNumber = 0

        binding.numberDisplay.text = "0"
        binding.instructionText.text = "Ready to Start Counting"
        binding.startButton.text = "Start Counting"
        binding.stopButton.isEnabled = false
        binding.stopButton.alpha = 0.5f
    }

    private fun startCountingAnimation() {
        countingRunnable = object : Runnable {
            override fun run() {
                if (isActive && currentNumber < maxCount) {
                    currentNumber++

                    // אנימציה של המספר
                    binding.numberDisplay.alpha = 0f
                    binding.numberDisplay.text = currentNumber.toString()
                    binding.numberDisplay.animate()
                        .alpha(1f)
                        .scaleX(1.3f)
                        .scaleY(1.3f)
                        .setDuration(300)
                        .withEndAction {
                            binding.numberDisplay.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start()
                        }
                        .start()

                    // עדכון ההוראות
                    when (currentNumber) {
                        1 -> binding.instructionText.text = "Take a deep breath"
                        3 -> binding.instructionText.text = "Feel yourself becoming calmer"
                        5 -> binding.instructionText.text = "You're halfway there"
                        7 -> binding.instructionText.text = "Keep focusing on the numbers"
                        9 -> binding.instructionText.text = "Almost finished"
                        10 -> {
                            binding.instructionText.text = "Excellent! You've reached 10"
                            completeCounting()
                            return
                        }
                    }

                    // המשך הספירה אחרי 1.5 שניות
                    handler.postDelayed(this, 1500)
                }
            }
        }
        handler.postDelayed(countingRunnable!!, 1000)
    }

    private fun completeCounting() {
        isActive = false
        binding.startButton.text = "Start Again"
        binding.stopButton.isEnabled = false
        binding.stopButton.alpha = 0.5f

        // אפקט סיום
        binding.numberDisplay.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .alpha(0.8f)
            .setDuration(1000)
            .withEndAction {
                showCompletionDialog()
            }
            .start()
    }

    private fun showCompletionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Counting Complete")
            .setMessage("Well done! You've successfully completed the counting exercise to 10. This simple practice helps center your mind, reduce anxiety, and improve focus in stressful moments.")
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Count Again") { dialog, _ ->
                dialog.dismiss()
                resetCounting()
            }
            .create()

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(countingRunnable ?: return)
    }
}