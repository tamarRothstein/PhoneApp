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
import com.example.myphoneapp.ui.activities.BreathingActivity

class EmergencyFragment : Fragment() {

    private var _binding: FragmentEmergencyBinding? = null
    private val binding get() = _binding!!

    // הרשאות לחיוג ואנשי קשר
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.CALL_PHONE] == true -> {
                // הרשאת חיוג ניתנה
            }
            permissions[Manifest.permission.READ_CONTACTS] == true -> {
                // הרשאת אנשי קשר ניתנה
            }
        }
    }

    // רשימת אנשי קשר חירום (נטען מהמכשיר)
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
        // חיוג חירום מהיר
        binding.call100Button.setOnClickListener {
            makeEmergencyCall("100")
        }

        binding.callPoliceButton.setOnClickListener {
            makeEmergencyCall("911") // או המספר המקומי
        }

        binding.callAmbulanceButton.setOnClickListener {
            makeEmergencyCall("101")
        }

        // עזרה עצמית מהירה
        binding.quickBreathingButton.setOnClickListener {
            // מעבר ישיר לתרגיל נשימה חירום
            val intent = Intent(requireContext(), BreathingActivity::class.java)
            intent.putExtra("emergency_mode", true)
            startActivity(intent)
        }

        binding.panicHelpButton.setOnClickListener {
            showPanicAttackHelp()
        }

        binding.findHelpButton.setOnClickListener {
            findNearbyHelp()
        }

        // אנשי קשר
        binding.contactFamily.setOnClickListener {
            callEmergencyContact("family")
        }

        binding.contactFriend.setOnClickListener {
            callEmergencyContact("friend")
        }

        binding.contactDoctor.setOnClickListener {
            callEmergencyContact("doctor")
        }

        binding.addContactButton.setOnClickListener {
            addEmergencyContact()
        }

        // SOS ומיקום
        binding.sendSosButton.setOnClickListener {
            sendEmergencySMS()
        }

        binding.shareLocationButton.setOnClickListener {
            shareCurrentLocation()
        }

        binding.callUberButton.setOnClickListener {
            callEmergencyTransport()
        }
    }

    private fun makeEmergencyCall(number: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // אם אין הרשאת חיוג, פתח את החייגן
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
            }
            startActivity(intent)
        }
    }

    private fun loadEmergencyContacts() {
        // טעינת אנשי קשר מהמכשיר או מהגדרות
        val prefs = requireContext().getSharedPreferences("emergency_prefs", android.content.Context.MODE_PRIVATE)

        val familyName = prefs.getString("family_contact_name", "Add Family Contact")
        val familyPhone = prefs.getString("family_contact_phone", "")
        val friendName = prefs.getString("friend_contact_name", "Add Friend Contact")
        val friendPhone = prefs.getString("friend_contact_phone", "")
        val doctorName = prefs.getString("doctor_contact_name", "Add Doctor Contact")
        val doctorPhone = prefs.getString("doctor_contact_phone", "")

        binding.contactFamilyName.text = familyName
        binding.contactFriendName.text = friendName
        binding.contactDoctorName.text = doctorName
    }

    private fun callEmergencyContact(type: String) {
        val prefs = requireContext().getSharedPreferences("emergency_prefs", android.content.Context.MODE_PRIVATE)
        val phoneNumber = when(type) {
            "family" -> prefs.getString("family_contact_phone", "")
            "friend" -> prefs.getString("friend_contact_phone", "")
            "doctor" -> prefs.getString("doctor_contact_phone", "")
            else -> ""
        }

        if (phoneNumber?.isNotEmpty() == true) {
            makeEmergencyCall(phoneNumber)
        } else {
            // אם אין איש קשר, הצע להוסיף
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
            // אם אין אפליקציית אנשי קשר
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

                // שמירת איש הקשר
                saveEmergencyContact(name, phone)
            }
        }
    }

    private fun saveEmergencyContact(name: String, phone: String) {
        val prefs = requireContext().getSharedPreferences("emergency_prefs", android.content.Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // לשמור כאיש קשר ראשון פנוי
        when {
            prefs.getString("family_contact_name", "") == "Add Family Contact" -> {
                editor.putString("family_contact_name", name)
                editor.putString("family_contact_phone", phone)
            }
            prefs.getString("friend_contact_name", "") == "Add Friend Contact" -> {
                editor.putString("friend_contact_name", name)
                editor.putString("friend_contact_phone", phone)
            }
            else -> {
                editor.putString("doctor_contact_name", name)
                editor.putString("doctor_contact_phone", phone)
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

    private fun showPanicAttackHelp() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Panic Attack - You're Safe")
            .setMessage("This feeling will pass. You are not in danger.\n\n1. Breathe slowly: 4 seconds in, 6 seconds out\n2. Name 5 things you can see\n3. Name 4 things you can touch\n4. Name 3 things you can hear\n5. This will pass in 10-20 minutes")
            .setPositiveButton("Start Breathing") { _, _ ->
                val intent = Intent(requireContext(), BreathingActivity::class.java)
                intent.putExtra("emergency_mode", true)
                startActivity(intent)
            }
            .setNegativeButton("Call Someone") { _, _ ->
                callEmergencyContact("family")
            }
            .show()
    }

    private fun findNearbyHelp() {
        // פתיחת מפות לחיפוש עזרה בקרבת מקום
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:0,0?q=hospital,police,pharmacy")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Maps app not available",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
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
        // שיתוף מיקום נוכחי
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

    private fun callEmergencyTransport() {
        // קריאה לאובר/גט או מונית
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://m.uber.com/")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // חלופה - חיוג למונית מקומית
            makeEmergencyCall("*6400") // מספר מוניות דוגמא
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