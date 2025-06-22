package com.example.myphoneapp.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myphoneapp.databinding.ActivityVoiceGuidanceBinding
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class VoiceGuidanceActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityVoiceGuidanceBinding
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent

    // Azure OpenAI config
    private val AZURE_BASE_URL = "https://thali-mc6wkmqv-swedencentral.cognitiveservices.azure.com/"
    private val DEPLOYMENT_NAME = "gpt-35-turbo"
    private val API_VERSION = "2025-01-01-preview"
    private val AZURE_API_KEY =
        "9dApBtatgkgLFPGFZH2SoTXxvseZNSv08fWiQ47LZz3vZ23EF7toJQQJ99BFACfhMk5XJ3w3AAAAACOGAYfN"

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startListening()
            else Toast.makeText(this, "Permission denied for microphone", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceGuidanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)
        checkTTSAvailability()
        setupSpeechRecognizer()

        binding.voiceText.text = "לחץ על Start ודבר כאשר תהיה מוכן."
        binding.btnBack.setOnClickListener { finish() }

        binding.btnContinue.text = "Start"
        binding.btnPause.text = "Finish"

        binding.btnContinue.setOnClickListener {
            checkMicrophonePermissionAndListen()
        }

        binding.btnPause.setOnClickListener {
            pauseGuidance()
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Toast.makeText(
                    this@VoiceGuidanceActivity,
                    "Speech recognition error: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.getOrNull(0) ?: ""
                binding.voiceText.text = "זיהוי:\n$spokenText"

                getAIResponse(spokenText) { hebrew, english ->
                    binding.voiceText.text = "עברית:\n$hebrew\n\nEnglish:\n$english"

                    val isHebrew =
                        Locale("he", "IL").let { it.language in spokenText.lowercase(Locale.ROOT) }

                    if (isHebrew) {
                        speakWithTTS(hebrew, "he-IL")
                        Handler(mainLooper).postDelayed({
                            speakWithTTS(english, "en-US")
                        }, 3000)
                    } else {
                        speakWithTTS(english, "en-US")
                        Handler(mainLooper).postDelayed({
                            speakWithTTS(hebrew, "he-IL")
                        }, 3000)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL")
        }
    }

    private fun checkMicrophonePermissionAndListen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startListening()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(this, "יש לאשר גישה למיקרופון להפעלת זיהוי קולי", Toast.LENGTH_LONG)
                    .show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startListening() {
        Toast.makeText(this, "מקשיב עכשיו...", Toast.LENGTH_SHORT).show()
        speechRecognizer.startListening(speechIntent)
    }

    private fun pauseGuidance() {
        tts?.stop()
        speechRecognizer.stopListening()
        Toast.makeText(this, "ההדרכה נעצרה", Toast.LENGTH_SHORT).show()
    }

    private fun getAIResponse(userInput: String, onResult: (String, String) -> Unit) {
        val client = OkHttpClient()

        val systemMessage = JSONObject()
            .put("role", "system")
            .put(
                "content",
                "אתה מדריך רגוע ותומך. ענה בצורה מרגיעה וקצרה בעברית. לאחר מכן, תרגם את אותה התשובה לאנגלית."
            )

        val userMessage = JSONObject()
            .put("role", "user")
            .put("content", userInput)

        val messages = JSONArray().put(systemMessage).put(userMessage)

        val jsonBody = JSONObject()
            .put("messages", messages)
            .put("temperature", 0.7)

        val mediaType = "application/json".toMediaType()
        val body = RequestBody.create(mediaType, jsonBody.toString())

        val request = Request.Builder()
            .url("${AZURE_BASE_URL}openai/deployments/$DEPLOYMENT_NAME/chat/completions?api-version=$API_VERSION")
            .addHeader("Content-Type", "application/json")
            .addHeader("api-key", AZURE_API_KEY)
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onResult("שגיאה משרת Azure: ${response.message}", "")
                    }
                    return@launch
                }

                val jsonResponse = JSONObject(response.body!!.string())
                val message = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                val parts = message.split("English:").map { it.trim() }
                val hebrewPart = parts.getOrNull(0) ?: message
                val englishPart = parts.getOrNull(1) ?: ""

                withContext(Dispatchers.Main) {
                    onResult(hebrewPart, englishPart)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("שגיאה כללית: ${e.localizedMessage}", "")
                }
            }
        }
    }

    // --- פונקציה חדשה לדיבור עם TTS ---
    private fun speakWithTTS(text: String, language: String) {
        val locale = when (language) {
            "he-IL" -> Locale("he", "IL")
            "en-US" -> Locale.US
            else -> Locale.getDefault()
        }
        val availability = tts?.isLanguageAvailable(locale)
        if (availability == TextToSpeech.LANG_AVAILABLE ||
            availability == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            availability == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
        ) {
            tts?.language = locale
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
        } else {
            Toast.makeText(this, "השפה אינה נתמכת במנוע TTS הנוכחי", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        speechRecognizer.destroy()
    }

    // --- עדכון onInit ---
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("he", "IL"))
            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            ttsReady = false
            Toast.makeText(this, "שגיאה באתחול מנוע הדיבור", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkTTSAvailability() {
        val intent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        startActivityForResult(intent, 1001)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Toast.makeText(this, "TTS אינו מותקן. מפנה להתקנה...", Toast.LENGTH_LONG).show()
                val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                startActivity(installIntent)
            }
        }
    }
}