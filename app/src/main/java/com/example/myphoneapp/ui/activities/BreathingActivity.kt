package com.example.myphoneapp.ui.activities

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myphoneapp.R
import com.example.myphoneapp.databinding.ActivityBreathingBinding

class BreathingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBreathingBinding
    private var breathingAnimator: ValueAnimator? = null
    private var isBreathing = false
    private var breathingCircle: android.view.View? = null
    private var cycleCount = 0
    private val maxCycles = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCircle()
        setupClickListeners()
        setupInitialState()
    }

    private fun setupCircle() {
        // עיגול גדול עם מסגרת יפה
        breathingCircle = android.view.View(this).apply {
            background = ContextCompat.getDrawable(this@BreathingActivity, R.drawable.breathing_circle_outline)
            layoutParams = FrameLayout.LayoutParams(280, 280).apply {
                gravity = android.view.Gravity.CENTER
            }
        }

        binding.circleContainer.addView(breathingCircle)
    }

    private fun setupInitialState() {
        binding.instructionText.text = "Ready to Start"
        binding.phaseText.text = "4-4-4-4 breathing technique"
        binding.cycleCounter.text = "Cycle: 0 / $maxCycles"
        binding.startButton.text = "Start Breathing"
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.startButton.setOnClickListener {
            if (!isBreathing) {
                startBreathing()
            } else {
                stopBreathing()
            }
        }

        binding.doneButton.setOnClickListener {
            finish()
        }
    }

    private fun startBreathing() {
        if (isBreathing) return

        isBreathing = true
        cycleCount = 0
        binding.startButton.text = "Stop Exercise"
        binding.phaseText.text = "Follow the circle and breathe slowly"

        breatheIn()
    }

    private fun stopBreathing() {
        breathingAnimator?.cancel()
        isBreathing = false
        binding.instructionText.text = "Exercise Stopped"
        binding.phaseText.text = "4-4-4-4 breathing technique"
        binding.startButton.text = "Start Breathing"

        // החזרת העיגול לגודל רגיל
        breathingCircle?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.alpha(0.7f)
            ?.setDuration(500)
            ?.start()
    }

    private fun breatheIn() {
        if (!isBreathing) return

        binding.instructionText.text = "Breathe In"
        binding.phaseText.text = "Through your nose"

        breathingCircle?.animate()
            ?.scaleX(1.8f)
            ?.scaleY(1.8f)
            ?.alpha(1f)
            ?.setDuration(4000) // 4 שניות
            ?.withEndAction {
                if (isBreathing) {
                    holdIn()
                }
            }
            ?.start()
    }

    private fun holdIn() {
        if (!isBreathing) return

        binding.instructionText.text = "Hold"
        binding.phaseText.text = "Keep the air in"

        binding.root.postDelayed({
            if (isBreathing) {
                breatheOut()
            }
        }, 4000) // 4 שניות החזקה
    }

    private fun breatheOut() {
        if (!isBreathing) return

        binding.instructionText.text = "Breathe Out"
        binding.phaseText.text = "Through your mouth"

        breathingCircle?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.alpha(0.7f)
            ?.setDuration(4000) // 4 שניות
            ?.withEndAction {
                if (isBreathing) {
                    holdOut()
                }
            }
            ?.start()
    }

    private fun holdOut() {
        if (!isBreathing) return

        binding.instructionText.text = "Hold"
        binding.phaseText.text = "Empty lungs"

        binding.root.postDelayed({
            if (isBreathing) {
                cycleCount++
                binding.cycleCounter.text = "Cycle: $cycleCount / $maxCycles"

                if (cycleCount >= maxCycles) {
                    completeExercise()
                } else {
                    breatheIn() // מתחיל מחזור חדש
                }
            }
        }, 4000) // 4 שניות החזקה
    }

    private fun completeExercise() {
        isBreathing = false
        binding.instructionText.text = "Well Done!"
        binding.phaseText.text = "Exercise completed successfully"
        binding.startButton.text = "Start Again"

        // אפקט סיום
        breathingCircle?.animate()
            ?.scaleX(1.2f)
            ?.scaleY(1.2f)
            ?.alpha(0.8f)
            ?.setDuration(1000)
            ?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        breathingAnimator?.cancel()
    }
}