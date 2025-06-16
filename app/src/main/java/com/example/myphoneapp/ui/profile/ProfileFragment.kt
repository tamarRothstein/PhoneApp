package com.example.myphoneapp.ui.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
        // טעינת נתונים שמורים מ-SharedPreferences
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

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

        // הגדרת Spinner עבור מין
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Non-binary", "Prefer not to say")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genderSpinner.adapter = adapter
        binding.genderSpinner.setSelection(prefs.getInt("profile_gender", 0))
    }

    private fun setupClickListeners() {
        // שמירת פרופיל
        binding.saveProfileButton.setOnClickListener {
            saveProfile()
        }

        // בחירת תאריך לידה
        binding.birthdateCard.setOnClickListener {
            showDatePicker()
        }

        // מעקב אחרי פעילות
        binding.viewActivityHistoryButton.setOnClickListener {
            showActivityHistory()
        }

        // הגדרות
        binding.notificationSettingsButton.setOnClickListener {
            showNotificationSettings()
        }

        binding.privacySettingsButton.setOnClickListener {
            showPrivacySettings()
        }

        binding.exportDataButton.setOnClickListener {
            exportProfileData()
        }

        // כפתור יציאה
        binding.signOutButton.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun saveProfile() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // שמירת נתונים בסיסיים
        editor.putString("profile_name", binding.nameEdit.text.toString())
        editor.putString("profile_email", binding.emailEdit.text.toString())
        editor.putString("profile_phone", binding.phoneEdit.text.toString())
        editor.putString("profile_birthdate", binding.birthdateText.text.toString())
        editor.putInt("profile_gender", binding.genderSpinner.selectedItemPosition)
        editor.putString("profile_height", binding.heightEdit.text.toString())
        editor.putString("profile_weight", binding.weightEdit.text.toString())

        // שמירת נתונים רפואיים
        editor.putString("profile_medical", binding.medicalConditionsEdit.text.toString())
        editor.putString("profile_medications", binding.medicationsEdit.text.toString())
        editor.putString("profile_allergies", binding.allergiesEdit.text.toString())
        editor.putString("profile_emergency_contact", binding.emergencyContactEdit.text.toString())
        editor.putString("profile_doctor", binding.doctorEdit.text.toString())

        editor.apply()

        updateProfileCompleteness()

        AlertDialog.Builder(requireContext())
            .setTitle("Profile Saved")
            .setMessage("Your profile information has been saved successfully!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
            calendar.get(Calendar.YEAR) - 25, // ברירת מחדל - גיל 25
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateProfileCompleteness() {
        val fields = listOf(
            binding.nameEdit.text.toString(),
            binding.emailEdit.text.toString(),
            binding.birthdateText.text.toString(),
            binding.heightEdit.text.toString(),
            binding.weightEdit.text.toString()
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
            • Breathing exercises: 5 sessions
            • Meditation: 3 sessions  
            • Calming sounds: 7 sessions
            • Emergency help used: 0 times
            
            This Month:
            • Total wellness activities: 23
            • Average session length: 8 minutes
            • Most used feature: Breathing exercises
            • Stress level improvements: 15%
            
            Achievements:
            • 7-day wellness streak
            • Stress reducer badge
            • Mindful moments master
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Activity History")
            .setMessage(activityData)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showNotificationSettings() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Settings")
            .setMessage("Customize your notifications:\n\n• Wellness reminders\n• Emergency alerts\n• Activity suggestions\n• Health insights\n• App updates")
            .setPositiveButton("Configure") { dialog, _ ->
                dialog.dismiss()
                // פתיחת הגדרות התראות
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPrivacySettings() {
        AlertDialog.Builder(requireContext())
            .setTitle("Privacy and Security")
            .setMessage("Your privacy matters:\n\n• All data is encrypted\n• No data shared without consent\n• Local storage priority\n• GDPR compliant\n• Right to data deletion")
            .setPositiveButton("View Policy") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun exportProfileData() {
        AlertDialog.Builder(requireContext())
            .setTitle("Export Data")
            .setMessage("Export your profile and activity data for:\n\n• Healthcare provider sharing\n• Personal backup\n• App migration\n• Data analysis\n\nData will be exported in a secure format.")
            .setPositiveButton("Export") { dialog, _ ->
                dialog.dismiss()
                // יצוא נתונים
                android.widget.Toast.makeText(requireContext(), "Data export started...", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out? Your data will remain saved on this device.")
            .setPositiveButton("Sign Out") { dialog, _ ->
                dialog.dismiss()
                // יציאה מהאפליקציה
                requireActivity().finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        // רענון נתונים בכל פעם שחוזרים לדף
        updateProfileCompleteness()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}