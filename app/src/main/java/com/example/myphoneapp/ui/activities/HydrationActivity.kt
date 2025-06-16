package com.example.myphoneapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityHydrationBinding

class HydrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHydrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHydrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCompleted.setOnClickListener {
            showCompletionDialog()
        }

        binding.btnRemindLater.setOnClickListener {
            showRemindLaterDialog()
        }
    }

    private fun showCompletionDialog() {
        com.example.myphoneapp.ui.common.CompletionDialog.showHydrationCompletion(
            this,
            onAnotherRound = { finish() }, // Go back to wellness options
            onFinish = { finishAffinity() } // Close all activities
        )
    }

    private fun showRemindLaterDialog() {
        com.example.myphoneapp.ui.common.CompletionDialog(
            this,
            "No Problem! ⏰",
            "We'll remind you later",
            "💙",
            onAnotherRound = { finish() },
            onFinish = { finishAffinity() }
        ).show()
    }
}