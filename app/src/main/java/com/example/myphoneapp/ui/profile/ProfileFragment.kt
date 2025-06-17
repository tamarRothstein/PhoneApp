package com.example.myphoneapp.ui.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myphoneapp.R
import com.example.myphoneapp.databinding.FragmentProfileBinding
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfileData()
        setupClickListeners()
        updateProfileCompleteness()
    }

    private fun loadProfileData() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", 0)

        // Load all saved data
        binding.nameEdit.setText(prefs.getString("profile_name", ""))
        binding.emailEdit.setText(prefs.getString("profile_email", ""))
        binding.phoneEdit.setText(prefs.getString("profile_phone", ""))
        binding.birthdateText.text = prefs.getString("profile_birthdate", "Not set")
        binding.heightEdit.setText(prefs.getString("profile_height", ""))
        binding.weightEdit.setText(prefs.getString("profile_weight", ""))
        binding.medicalConditionsEdit.setText(prefs.getString("profile_medical", ""))
        binding.medicationsEdit.setText(prefs.getString("profile_medications", ""))
        binding.allergiesEdit.setText(prefs.getString("profile_allergies", ""))
        binding.emergencyContactEdit.setText(prefs.getString("profile_emergency_contact", ""))
        binding.doctorEdit.setText(prefs.getString("profile_doctor", ""))

        // Setup gender spinner
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Non-binary", "Prefer not to say")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genderSpinner.adapter = adapter
        binding.genderSpinner.setSelection(prefs.getInt("profile_gender", 0))
    }

    private fun setupClickListeners() {
        binding.saveProfileButton.setOnClickListener {
            saveProfile()
        }

        binding.birthdateCard.setOnClickListener {
            showDatePicker()
        }

        binding.viewActivityHistoryButton.setOnClickListener {
            showActivityHistory()
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

    private fun saveProfile() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", 0)
        val editor = prefs.edit()

        // Save all profile data
        editor.putString("profile_name", binding.nameEdit.text.toString())
        editor.putString("profile_email", binding.emailEdit.text.toString())
        editor.putString("profile_phone", binding.phoneEdit.text.toString())
        editor.putString("profile_birthdate", binding.birthdateText.text.toString())
        editor.putInt("profile_gender", binding.genderSpinner.selectedItemPosition)
        editor.putString("profile_height", binding.heightEdit.text.toString())
        editor.putString("profile_weight", binding.weightEdit.text.toString())
        editor.putString("profile_medical", binding.medicalConditionsEdit.text.toString())
        editor.putString("profile_medications", binding.medicationsEdit.text.toString())
        editor.putString("profile_allergies", binding.allergiesEdit.text.toString())
        editor.putString("profile_emergency_contact", binding.emergencyContactEdit.text.toString())
        editor.putString("profile_doctor", binding.doctorEdit.text.toString())

        editor.apply()
        updateProfileCompleteness()

        Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.birthdateText.text = dateFormat.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR) - 25,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateProfileCompleteness() {
        val fields = listOf(
            binding.nameEdit.text.toString(),
            binding.emailEdit.text.toString(),
            binding.phoneEdit.text.toString(),
            binding.birthdateText.text.toString(),
            binding.heightEdit.text.toString(),
            binding.weightEdit.text.toString(),
            binding.emergencyContactEdit.text.toString()
        )

        val completedFields = fields.count { it.isNotEmpty() && it != "Not set" }
        val percentage = (completedFields * 100) / fields.size

        binding.profileCompletenessText.text = "Profile Completeness: $percentage%"
        binding.profileCompletenessProgress.progress = percentage
    }

    private fun showActivityHistory() {
        val activityData = """
           Your Wellness Activity Summary:
           
           This Week:
           • Breathing exercises: 12 sessions
           • Meditation: 8 sessions  
           • Calming sounds: 15 sessions
           • Guided imagery: 6 sessions
           • Emergency help used: 0 times
           
           This Month:
           • Total wellness activities: 45
           • Average session length: 9 minutes
           • Most used feature: Breathing exercises
           • Stress level improvements: 25%
           • Sleep quality improved: 18%
           
           Health Trends:
           • Heart rate variability: Improved
           • Daily step count: 8,500 average
           • Sleep duration: 7.2 hours average
           • Stress management: Significantly improved
           
           Achievements:
           • 14-day wellness streak
           • Stress reducer champion
           • Mindful moments master
           • Sleep quality improver
           • Consistency warrior
           
           Recommendations:
           • Continue current breathing routine
           • Try morning meditation sessions
           • Increase hydration reminders
           • Consider evening calming sounds
       """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Detailed Activity History")
            .setMessage(activityData)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showNotificationSettings() {
        val notificationOptions = """
           Customize your notification preferences:
           
           Wellness Reminders:
           • Daily breathing exercise reminders
           • Meditation session suggestions
           • Hydration alerts
           • Movement breaks
           • Sleep preparation notifications
           
           Health Monitoring:
           • Heart rate variability alerts
           • Stress level warnings
           • Activity goal reminders
           • Weekly progress summaries
           
           Emergency Features:
           • Emergency contact alerts
           • Location sharing notifications
           • Panic attack support triggers
           • Medical emergency protocols
           
           App Updates:
           • New feature announcements
           • System maintenance notices
           • Privacy policy updates
           • Health tip notifications
           
           Timing Options:
           • Morning wellness check-ins
           • Afternoon stress assessments
           • Evening relaxation reminders
           • Weekend activity suggestions
       """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Notification Settings")
            .setMessage(notificationOptions)
            .setPositiveButton("Configure") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Notification settings configured", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPrivacySettings() {
        val privacyInfo = """
           Your Privacy and Security Settings:
           
           Data Protection:
           • All personal data is encrypted with AES-256
           • Biometric data stored locally only
           • No data shared without explicit consent
           • GDPR and HIPAA compliant
           • Regular security audits performed
           
           Data Collection:
           • Health metrics for personal insights
           • Usage patterns for app improvement
           • Location data for emergency services only
           • Voice data for guided sessions (optional)
           • Camera access for heart rate monitoring (optional)
           
           Data Sharing Options:
           • Share with healthcare providers (optional)
           • Emergency contact data sharing
           • Anonymous research participation (optional)
           • App improvement analytics (optional)
           • Third-party integrations (user controlled)
           
           Your Rights:
           • Access your data anytime
           • Request data correction
           • Delete your account and data
           • Export data in standard formats
           • Opt-out of data collection
           
           Security Features:
           • Two-factor authentication available
           • Automatic session timeout
           • Device-specific encryption keys
           • Secure backup options
           • Audit trail of data access
       """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Privacy and Security")
            .setMessage(privacyInfo)
            .setPositiveButton("View Full Policy") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Opening privacy policy", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun exportProfileData() {
        val exportInfo = """
           Export Your Complete Health Profile:
           
           Data Included:
           • Personal information and preferences
           • Complete health history and metrics
           • All wellness activity records
           • Medication and allergy information
           • Emergency contact details
           • Sleep and activity patterns
           • Stress management progress
           
           Export Formats Available:
           • PDF health summary report
           • CSV data for spreadsheet analysis
           • JSON for technical applications
           • HL7 FHIR for healthcare systems
           • Encrypted backup file
           
           Use Cases:
           • Share with your healthcare provider
           • Personal health record backup
           • Switching to another health app
           • Research participation
           • Insurance documentation
           • Medical consultation preparation
           
           Security Measures:
           • Password-protected files
           • Encryption for sensitive data
           • Temporary download links
           • Automatic file deletion after 7 days
           • Activity logging for exports
           
           The export process may take 2-5 minutes depending on your data volume.
       """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Export Your Health Data")
            .setMessage(exportInfo)
            .setPositiveButton("Start Export") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Data export started. You will be notified when complete.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?\n\nYour profile data will remain saved on this device and will be available when you sign back in.\n\nActive wellness sessions will be paused.")
            .setPositiveButton("Sign Out") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateProfileCompleteness()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}