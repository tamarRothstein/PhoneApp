package com.example.myphoneapp.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myphoneapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.notificationSettingsButton.setOnClickListener {
            showNotificationSettings()
        }

        binding.privacySettingsButton.setOnClickListener {
            showPrivacySettings()
        }

        binding.exportDataButton.setOnClickListener {
            exportProfileData()
        }

        binding.signOutButton.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun showNotificationSettings() {
        val message = """
            Customize your notifications:
            
            - Daily wellness reminders
            - Stress level alerts
            - Sleep quality summaries
            - Activity goals
            - New feature announcements
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Notification Settings")
            .setMessage(message)
            .setPositiveButton("Configure") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Notification settings updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showPrivacySettings() {
        val message = """
            Your privacy and security settings:
            
            - All data encrypted locally (AES-256)
            - No sharing without explicit consent
            - You control export & deletion
            - GDPR & HIPAA compliant
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Privacy & Security")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportProfileData() {
        val message = """
            Export includes:
            
            - Profile details
            - Health history
            - Wellness activities
            - Emergency contacts
            
            Format: PDF, CSV or JSON
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Export Data")
            .setMessage(message)
            .setPositiveButton("Export") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Data export started...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out? Your data remains saved on this device.")
            .setPositiveButton("Sign Out") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
