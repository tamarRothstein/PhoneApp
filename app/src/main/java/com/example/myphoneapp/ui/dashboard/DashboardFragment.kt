package com.example.myphoneapp.ui.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myphoneapp.databinding.FragmentDashboardBinding
import com.example.myphoneapp.ui.activities.VoiceGuidanceActivity
import com.example.myphoneapp.ui.activities.BreathingActivity
import com.example.myphoneapp.R

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

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

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            dashboardReceiver,
            IntentFilter("com.example.myphoneapp.UPDATE_DASHBOARD")
        )
    }

    private fun setupUI() {
        binding.apply {
            heartRateValue.text = getString(R.string.heart_rate_default)
            stressLevelValue.text = getString(R.string.stress_level_low)
            stepsValue.text = getString(R.string.steps_default)
            currentStatusText.text = getString(R.string.status_great_today)

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

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(dashboardReceiver)
        _binding = null
    }
}
