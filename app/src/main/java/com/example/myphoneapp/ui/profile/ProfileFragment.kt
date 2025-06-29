package com.example.myphoneapp.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myphoneapp.databinding.FragmentProfileBinding
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlertDialog
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.example.myphoneapp.R

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoriteContactsContainer: LinearLayout

    private val conditionsList = listOf("Diabetes", "Hypertension", "Asthma", "COPD", "Depression", "Anxiety")
    private val medicationsList = listOf("Paracetamol", "Ibuprofen", "Aspirin", "Metformin", "Amoxicillin")
    private val allergiesList = listOf("Peanuts", "Shellfish", "Penicillin", "Pollen", "Dust Mites")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfileData()
        setupAutoCompleteFields()
        setupClickListeners()
        updateProfileCompleteness()
    }

    private fun setupAutoCompleteFields() {
        binding.medicalConditionsEdit.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, conditionsList))
        binding.medicationsEdit.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, medicationsList))
        binding.allergiesEdit.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allergiesList))
    }

    private fun setupClickListeners() {
        binding.birthdateCard.setOnClickListener { showDatePicker() }
        binding.saveProfileButton.setOnClickListener { saveProfile() }
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(com.example.myphoneapp.R.id.action_profileFragment_to_settingsFragment)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.birthdateText.text = format.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveProfile() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", 0).edit()
        prefs.putString("profile_name", binding.nameEdit.text.toString())
        prefs.putString("profile_email", binding.emailEdit.text.toString())
        prefs.putString("profile_phone", binding.phoneEdit.text.toString())
        prefs.putString("profile_birthdate", binding.birthdateText.text.toString())
        prefs.putInt("profile_gender", binding.genderSpinner.selectedItemPosition)
        prefs.putString("profile_height", binding.heightEdit.text.toString())
        prefs.putString("profile_weight", binding.weightEdit.text.toString())
        prefs.putString("profile_medical", binding.medicalConditionsEdit.text.toString())
        prefs.putString("profile_medications", binding.medicationsEdit.text.toString())
        prefs.putString("profile_allergies", binding.allergiesEdit.text.toString())
        prefs.apply()

        updateProfileCompleteness()
        Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun loadProfileData() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", 0)
        binding.nameEdit.setText(prefs.getString("profile_name", ""))
        binding.emailEdit.setText(prefs.getString("profile_email", ""))
        binding.phoneEdit.setText(prefs.getString("profile_phone", ""))
        binding.birthdateText.text = prefs.getString("profile_birthdate", "Not set")
        binding.genderSpinner.setSelection(prefs.getInt("profile_gender", 0))
        binding.heightEdit.setText(prefs.getString("profile_height", ""))
        binding.weightEdit.setText(prefs.getString("profile_weight", ""))
        binding.medicalConditionsEdit.setText(prefs.getString("profile_medical", ""), false)
        binding.medicationsEdit.setText(prefs.getString("profile_medications", ""), false)
        binding.allergiesEdit.setText(prefs.getString("profile_allergies", ""), false)
    }

    private fun updateProfileCompleteness() {
        val fields = listOf(
            binding.nameEdit.text.toString(),
            binding.emailEdit.text.toString(),
            binding.phoneEdit.text.toString(),
            binding.birthdateText.text.toString(),
            binding.heightEdit.text.toString(),
            binding.weightEdit.text.toString(),
        )

        val completed = fields.count { it.isNotEmpty() && it != "Not set" }
        val percent = (completed * 100) / fields.size
        binding.profileCompletenessText.text = "Profile Completeness: $percent%"
        binding.profileCompletenessProgress.progress = percent
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveFavoriteContact(contact: String) {
        val prefs = requireContext().getSharedPreferences("profile_prefs", 0)
        val set = prefs.getStringSet("favorite_contacts", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(contact)
        prefs.edit().putStringSet("favorite_contacts", set).apply()
    }

    private fun loadFavoriteContacts() {
        favoriteContactsContainer.removeAllViews()
        val prefs = requireContext().getSharedPreferences("profile_prefs", 0)
        val contacts = prefs.getStringSet("favorite_contacts", setOf()) ?: setOf()

        for (contact in contacts) {
            val button = MaterialButton(requireContext()).apply {
                text = contact
                setBackgroundColor(resources.getColor(R.color.primary_sage, null))
                setTextColor(resources.getColor(R.color.text_white, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8
                }
            }
            favoriteContactsContainer.addView(button)
        }
    }

    private fun showAddFavoriteDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_favorite, null)
        val nameEdit = dialogView.findViewById<EditText>(R.id.editFavoriteName)
        val phoneEdit = dialogView.findViewById<EditText>(R.id.editFavoritePhone)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Favorite Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEdit.text.toString()
                val phone = phoneEdit.text.toString()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    saveFavoriteContact("$name - $phone")
                    loadFavoriteContacts()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
