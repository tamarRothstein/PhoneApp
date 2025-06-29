package com.example.myphoneapp.ui.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myphoneapp.databinding.FragmentDashboardBinding
import com.example.myphoneapp.ui.activities.VoiceGuidanceActivity
import com.example.myphoneapp.ui.activities.BreathingActivity
import com.example.myphoneapp.R

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler()

    private val dashboardReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val heartRate = intent?.getStringExtra("heart_rate")

            heartRate?.let {
                binding.heartRateValue.text = it

                val rate = it.toIntOrNull() ?: 0
                val (stressLevel, statusMessage) = when {
                    rate < 60 -> "Low" to "You're calm and relaxed"
                    rate in 60..90 -> "Medium" to "You're doing fine"
                    else -> "High" to "Take a deep breath and rest"
                }

                binding.stressLevelValue.text = stressLevel
                binding.currentStatusText.text = statusMessage
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        checkForFirebaseAlert()
        startPeriodicDashboardUpdate()

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            dashboardReceiver,
            IntentFilter("com.example.myphoneapp.UPDATE_DASHBOARD")
        )
    }

    private fun setupUI() {
        val prefs = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
        val heartRate = prefs.getInt("heartRate", 83)
        val stressLevelPref = prefs.getString("stressLevel", null)
        val steps = prefs.getString("steps", getString(R.string.steps_default))

        binding.apply {
            heartRateValue.text = heartRate.toString()

            val (stressLevel, statusMessage) = when {
                heartRate < 60 -> "Low" to "You're calm and relaxed"
                heartRate in 60..90 -> "Medium" to "You're doing fine"
                else -> "High" to "Take a deep breath and rest"
            }

            stressLevelValue.text = stressLevelPref ?: stressLevel
            currentStatusText.text = statusMessage
            stepsValue.text = steps

            btnStartVoiceGuidance.setOnClickListener {
                val intent = Intent(requireContext(), VoiceGuidanceActivity::class.java)
                startActivity(intent)
            }

            btnStartBreathing.setOnClickListener {
                val intent = Intent(requireContext(), BreathingActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun checkForFirebaseAlert() {
        val prefs = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
        val shouldShow = prefs.getBoolean("should_show_alert", false)
        val alertMessage = prefs.getString("last_alert_message", "Take a deep breath")

        if (shouldShow) {
            AlertDialog.Builder(requireContext())
                .setTitle("Wellness Alert")
                .setMessage(alertMessage)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

            prefs.edit().putBoolean("should_show_alert", false).apply()
        }
    }

    private fun startPeriodicDashboardUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                updateDashboardData()
                handler.postDelayed(this, 60_000) // כל דקה
            }
        })
    }

    private fun updateDashboardData() {
        val prefs = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
        val heartRate = prefs.getInt("heartRate", 83)
        val stressLevelPref = prefs.getString("stressLevel", null)
        val steps = prefs.getString("steps", getString(R.string.steps_default))

        binding.apply {
            heartRateValue.text = heartRate.toString()

            val (stressLevel, statusMessage) = when {
                heartRate < 60 -> "Low" to "You're calm and relaxed"
                heartRate in 60..90 -> "Medium" to "You're doing fine"
                else -> "High" to "Take a deep breath and rest"
            }

            stressLevelValue.text = stressLevelPref ?: stressLevel
            currentStatusText.text = statusMessage
            stepsValue.text = steps
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(dashboardReceiver)
        _binding = null
    }
}