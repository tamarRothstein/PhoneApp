package com.example.myphoneapp.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myphoneapp.R
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
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var OPENAI_API_KEY: String

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startListening()
            } else {
                Toast.makeText(this, "Permission denied for microphone", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceGuidanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        OPENAI_API_KEY = getSecretValue(this, "OPENAI_API_KEY")

        tts = TextToSpeech(this, this)
        setupSpeechRecognizer()

        binding.voiceText.text = "Press 'Start' and speak when ready."

        binding.btnBack.setOnClickListener { finish() }

        binding.btnContinue.text = "Start"
        binding.btnPause.text = "Finish"

        binding.btnContinue.setOnClickListener { checkMicrophonePermissionAndListen() }

        binding.btnPause.setOnClickListener { pauseGuidance() }
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
                Toast.makeText(this@VoiceGuidanceActivity, "Speech recognition error: $error", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.getOrNull(0) ?: ""
                binding.voiceText.text = "You said:\n$spokenText"

                getAIResponse(spokenText) { aiResponse ->
                    binding.voiceText.text = aiResponse
                    tts?.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, "AIResponse")

                    // Example "agent" action: switch screen if AI suggests breathing
                    val lower = aiResponse.lowercase()
                    when {
                        "breathe" in lower || "נשום" in lower -> {
                            startActivity(Intent(this@VoiceGuidanceActivity, BreathingActivity::class.java))
                        }
                        "music" in lower || "מוזיקה" in lower -> {
                            startActivity(Intent(this@VoiceGuidanceActivity, CalmingSoundsActivity::class.java))
                        }
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
        }
    }

    private fun checkMicrophonePermissionAndListen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startListening()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(this, "Microphone permission is required for speech recognition", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startListening() {
        Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show()
        speechRecognizer.startListening(speechIntent)
    }

    private fun pauseGuidance() {
        tts?.stop()
        speechRecognizer.stopListening()
        Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show()
    }

    private fun getAIResponse(userInput: String, onResult: (String) -> Unit) {
        val client = OkHttpClient()

        val messages = JSONArray().put(
            JSONObject()
                .put("role", "user")
                .put("content", "You are a calm and supportive assistant. Respond soothingly to: $userInput")
        )

        val jsonBody = JSONObject()
            .put("model", "gpt-3.5-turbo")
            .put("messages", messages)

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $OPENAI_API_KEY")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onResult("Error: ${response.message}")
                    }
                    return@launch
                }
                val responseJson = JSONObject(response.body!!.string())
                val aiText = responseJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                withContext(Dispatchers.Main) {
                    onResult(aiText.trim())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("Exception: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun getSecretValue(context: Context, key: String): String {
        val inputStream = context.resources.openRawResource(R.raw.secret)
        val properties = Properties()
        properties.load(inputStream)
        return properties.getProperty(key) ?: ""
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported for TTS", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "TTS Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        speechRecognizer.destroy()
    }
}
