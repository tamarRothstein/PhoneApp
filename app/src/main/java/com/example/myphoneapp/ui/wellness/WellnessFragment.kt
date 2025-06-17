package com.example.myphoneapp.ui.alert

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myphoneapp.databinding.FragmentWellnessBinding
import com.example.myphoneapp.ui.activities.*

class WellnessFragment : Fragment() {

    private var _binding: FragmentWellnessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWellnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnVoiceGuidance.setOnClickListener {
            startActivity(Intent(requireContext(), VoiceGuidanceActivity::class.java))
        }

        binding.btnBreathing.setOnClickListener {
            startActivity(Intent(requireContext(), BreathingActivity::class.java))
        }

        binding.btnShakeOut.setOnClickListener {
            startActivity(Intent(requireContext(), ShakeOutActivity::class.java))
        }

        binding.btnMeditation.setOnClickListener {
            startActivity(Intent(requireContext(), MeditationActivity::class.java))
        }

        binding.btnCalmingSounds.setOnClickListener {
            startActivity(Intent(requireContext(), CalmingSoundsActivity::class.java))
        }

        binding.btnHeartbeat.setOnClickListener {
            startActivity(Intent(requireContext(), HeartbeatActivity::class.java))
        }

        binding.btnCounting.setOnClickListener {
            startActivity(Intent(requireContext(), CountingActivity::class.java))
        }

        binding.btnHydration.setOnClickListener {
            startActivity(Intent(requireContext(), HydrationActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
