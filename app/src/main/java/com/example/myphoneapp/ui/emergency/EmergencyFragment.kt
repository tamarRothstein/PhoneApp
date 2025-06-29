package com.example.myphoneapp.ui.emergency

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myphoneapp.databinding.FragmentEmergencyBinding

class EmergencyFragment : Fragment() {

    private var _binding: FragmentEmergencyBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    private val emergencyContacts = mutableListOf<EmergencyContact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()
        loadEmergencyContacts()
        setupClickListeners()
    }

    private fun checkPermissions() {
        val callPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
        val contactsPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)

        if (callPermission != PackageManager.PERMISSION_GRANTED ||
            contactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS
            ))
        }
    }

    private fun setupClickListeners() {
        binding.call100Button.setOnClickListener {
            makeEmergencyCall("100")
        }

        binding.callAmbulanceButton.setOnClickListener {
            makeEmergencyCall("101")
        }

        binding.contactFamily.setOnClickListener {
            callEmergencyContact("family")
        }

        binding.contactFriend.setOnClickListener {
            callEmergencyContact("friend")
        }

        binding.addContactButton.setOnClickListener {
            addEmergencyContact()
        }

        binding.sendSosButton.setOnClickListener {
            sendEmergencySMS()
        }

        binding.shareLocationButton.setOnClickListener {
            shareCurrentLocation()
        }
    }

    private fun makeEmergencyCall(number: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
            }
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
            }
            startActivity(intent)
        }
    }

    private fun loadEmergencyContacts() {
        val prefs = requireContext().getSharedPreferences("emergency_prefs", android.content.Context.MODE_PRIVATE)

        val familyName = prefs.getString("family_contact_name", "Add Family Contact")
        val friendName = prefs.getString("friend_contact_name", "Add Friend Contact")

        binding.contactFamilyName.text = familyName
        binding.contactFriendName.text = friendName
    }

    private fun callEmergencyContact(type: String) {
        val prefs = requireContext().getSharedPreferences("emergency_prefs", android.content.Context.MODE_PRIVATE)
        val phoneNumber = when(type) {
            "family" -> prefs.getString("family_contact_phone", "")
            "friend" -> prefs.getString("friend_contact_phone", "")
            else -> ""
        }

        if (phoneNumber?.isNotEmpty() == true) {
            makeEmergencyCall(phoneNumber)
        } else {
            addEmergencyContact()
        }
    }

    private fun addEmergencyContact() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }

        try {
            contactPickerLauncher.launch(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Please add emergency contacts in your phone's contacts app",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    private val contactPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                getContactInfo(uri)
            }
        }
    }

    private fun getContactInfo(uri: Uri) {
        val cursor: Cursor? = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val name = it.getString(nameIndex)
                val phone = it.getString(phoneIndex)

                saveEmergencyContact(name, phone)
            }
        }
    }

    private fun saveEmergencyContact(name: String, phone: String) {
        val prefs = requireContext().getSharedPreferences("emergency_prefs", android.content.Context.MODE_PRIVATE)
        val editor = prefs.edit()

        when {
            prefs.getString("family_contact_name", "") == "Add Family Contact" -> {
                editor.putString("family_contact_name", name)
                editor.putString("family_contact_phone", phone)
            }
            else -> {
                editor.putString("friend_contact_name", name)
                editor.putString("friend_contact_phone", phone)
            }
        }

        editor.apply()
        loadEmergencyContacts()

        android.widget.Toast.makeText(
            requireContext(),
            "Emergency contact added: $name",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun sendEmergencySMS() {
        val prefs = requireContext().getSharedPreferences("emergency_prefs", android.content.Context.MODE_PRIVATE)
        val familyPhone = prefs.getString("family_contact_phone", "")

        if (familyPhone?.isNotEmpty() == true) {
            val message = "🆘 EMERGENCY: I need help. Please check on me. Sent from my wellness app."

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$familyPhone")
                putExtra("sms_body", message)
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "SMS app not available",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            android.widget.Toast.makeText(
                requireContext(),
                "Please add an emergency contact first",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareCurrentLocation() {
        val locationMessage = "📍 I need help at my current location. Please check on me."

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, locationMessage)
            putExtra(Intent.EXTRA_SUBJECT, "Emergency Location")
        }

        try {
            startActivity(Intent.createChooser(intent, "Share Location"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Unable to share location",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class EmergencyContact(
        val name: String,
        val phone: String,
        val type: String
    )
}