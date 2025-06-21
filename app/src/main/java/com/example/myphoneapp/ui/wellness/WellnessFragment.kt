package com.example.myphoneapp.ui.wellness

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myphoneapp.databinding.FragmentWellnessBinding
import com.example.myphoneapp.ui.activities.BreathingActivity
import com.example.myphoneapp.ui.activities.CalmingSoundsActivity
import com.example.myphoneapp.ui.activities.ShakeOutActivity
import com.example.myphoneapp.ui.activities.VoiceGuidanceActivity

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

        binding.btnVoiceGuidance.setOnClickListener {
            startActivity(Intent(requireContext(), VoiceGuidanceActivity::class.java))
        }

        binding.btnBreathing.setOnClickListener {
            startActivity(Intent(requireContext(), BreathingActivity::class.java))
        }

        binding.btnShakeOut.setOnClickListener {
            startActivity(Intent(requireContext(), ShakeOutActivity::class.java))
        }

        binding.btnCalmingSounds.setOnClickListener {
            startActivity(Intent(requireContext(), CalmingSoundsActivity::class.java))
        }

        // ❗ השורות הבאות הועברו להערה כי ה־IDs שלהן ב־XML נמצאות בהערת <!-- ... -->
        // לכן אם תשאירי אותן בקוד, הן גורמות לשגיאת קומפילציה.

        /*
        binding.btnMeditation.setOnClickListener {
            // פעולה עבור Guided Imagery
        }

        binding.btnHeartbeat.setOnClickListener {
            // פעולה עבור Listen to Your Heartbeat
        }

        binding.btnCounting.setOnClickListener {
            // פעולה עבור Count to 10
        }

        binding.btnHydration.setOnClickListener {
            // פעולה עבור Drink Water
        }
        */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
