package com.example.myphoneapp.ui.common

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.example.myphoneapp.R

class CompletionDialog(
    context: Context,
    private val title: String,
    private val message: String,
    private val icon: String,
    private val onAnotherRound: () -> Unit,
    private val onFinish: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_completion)

        // Make dialog background transparent and set proper size
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupUI()
        setupClickListeners()

        setCancelable(false)
    }

    private fun setupUI() {
        findViewById<TextView>(R.id.success_icon).text = icon
        findViewById<TextView>(R.id.dialog_title).text = title
        findViewById<TextView>(R.id.dialog_message).text = message
    }

    private fun setupClickListeners() {
        findViewById<MaterialButton>(R.id.btn_another_round).setOnClickListener {
            onAnotherRound()
            dismiss()
        }

        findViewById<MaterialButton>(R.id.btn_finish).setOnClickListener {
            onFinish()
            dismiss()
        }
    }

    companion object {
        fun showBreathingCompletion(context: Context, onAnotherRound: () -> Unit, onFinish: () -> Unit) {
            CompletionDialog(
                context,
                "Amazing!",
                "You feel more centered now",
                "💙",
                onAnotherRound,
                onFinish
            ).show()
        }

        fun showCountingCompletion(context: Context, onAnotherRound: () -> Unit, onFinish: () -> Unit) {
            CompletionDialog(
                context,
                "Perfect!",
                "You're more focused now",
                "✨",
                onAnotherRound,
                onFinish
            ).show()
        }

        fun showMeditationCompletion(context: Context, onAnotherRound: () -> Unit, onFinish: () -> Unit) {
            CompletionDialog(
                context,
                "Wonderful!",
                "You found your inner peace",
                "🧘‍♀️",
                onAnotherRound,
                onFinish
            ).show()
        }

        fun showVoiceGuidanceCompletion(context: Context, onAnotherRound: () -> Unit, onFinish: () -> Unit) {
            CompletionDialog(
                context,
                "Beautiful!",
                "You're stronger than you know",
                "🌟",
                onAnotherRound,
                onFinish
            ).show()
        }

        fun showHydrationCompletion(context: Context, onAnotherRound: () -> Unit, onFinish: () -> Unit) {
            CompletionDialog(
                context,
                "Great!",
                "Your body thanks you",
                "💧",
                onAnotherRound,
                onFinish
            ).show()
        }
    }
}