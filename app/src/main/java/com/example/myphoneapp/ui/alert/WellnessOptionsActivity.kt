package com.example.myphoneapp.ui.alert

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityWellnessOptionsBinding
import com.example.myphoneapp.ui.activities.*

class WellnessOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWellnessOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWellnessOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnVoiceGuidance.setOnClickListener {
            startActivity(Intent(this, VoiceGuidanceActivity::class.java))
        }

        binding.btnBreathing.setOnClickListener {
            startActivity(Intent(this, BreathingActivity::class.java))
        }

        binding.btnShakeOut.setOnClickListener {
            startActivity(Intent(this, ShakeOutActivity::class.java))
        }

        binding.btnMeditation.setOnClickListener {
            startActivity(Intent(this, MeditationActivity::class.java))
        }

        binding.btnCalmingSounds.setOnClickListener {
            startActivity(Intent(this, CalmingSoundsActivity::class.java))
        }

        binding.btnHeartbeat.setOnClickListener {
            startActivity(Intent(this, HeartbeatActivity::class.java))
        }

        binding.btnCounting.setOnClickListener {
            startActivity(Intent(this, CountingActivity::class.java))
        }

        binding.btnHydration.setOnClickListener {
            startActivity(Intent(this, HydrationActivity::class.java))
        }
    }
}