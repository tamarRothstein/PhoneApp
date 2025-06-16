package com.example.myphoneapp.ui.activities

import android.Manifest
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
import com.example.myphoneapp.databinding.ActivityVoiceGuidanceBinding
import java.util.*

class VoiceGuidanceActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityVoiceGuidanceBinding

    private var isGuidanceActive = false
    private var currentStep = 0

    private var tts: TextToSpeech? = null
    private lateinit var currentLocale: Locale

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent

    // מיפוי של השלבים בשפות שונות (תוכל להוסיף שפות נוספות)
    private val guidanceStepsMap = mapOf(
        "en" to listOf(
            "I'm here with you right now. You're not alone in this moment.",
            "Take a deep breath with me... In through your nose... and slowly out through your mouth.",
            "You're doing great. Let's take another breath together... breathe in... and slowly release.",
            "Feel your body relaxing. Notice how your shoulders drop and your muscles soften.",
            "You are safe. You are strong. This feeling will pass, and you will be okay.",
            "Let's take one more deep breath together... in... and out. You've done wonderfully."
        ),
        "he" to listOf(
            "אני כאן איתך ברגע הזה. את לא לבד ברגע הזה.",
            "קחי נשימה עמוקה איתי... נשימה דרך האף... ונשיפה איטית דרך הפה.",
            "את עושה עבודה נהדרת. בואי ניקח נשימה נוספת ביחד... נשימה פנימה... ושחרור איטי.",
            "הרגישי איך הגוף נרגע. שימי לב איך הכתפיים יורדות והשרירים נרפים.",
            "את בטוחה. את חזקה. הרגשה זו תעבור, ואת תהיי בסדר.",
            "בואי ניקח נשימה עמוקה אחרונה... פנימה... ויוצא. עשית עבודה נפלאה."
        ),
        "ru" to listOf(
            "Я рядом с тобой прямо сейчас. Ты не одна в этот момент.",
            "Сделай глубокий вдох вместе со мной... вдох через нос... и медленный выдох через рот.",
            "Ты молодец. Давай сделаем ещё один вдох вместе... вдох... и медленно выдохни.",
            "Почувствуй, как расслабляется твое тело. Обрати внимание, как опускаются плечи и мышцы становятся мягче.",
            "Ты в безопасности. Ты сильна. Это чувство пройдет, и с тобой всё будет хорошо.",
            "Сделаем ещё один глубокий вдох вместе... вдох... и выдох. Ты молодец."
        )
    )

    private lateinit var guidanceSteps: List<String>

    // בקשת הרשאת מיקרופון דינמית
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

        // קביעת שפה לפי המכשיר והטקסטים
        setupLocaleAndSteps()

        // הפעלת TTS
        tts = TextToSpeech(this, this)

        // הגדרת SpeechRecognizer
        setupSpeechRecognizer()

        setupUI()
        setupClickListeners()
    }

    private fun setupLocaleAndSteps() {
        val deviceLang = Locale.getDefault().language
        currentLocale = when (deviceLang) {
            "he" -> Locale("he", "IL")
            "ru" -> Locale("ru", "RU")
            else -> Locale.US
        }
        guidanceSteps = guidanceStepsMap[deviceLang] ?: guidanceStepsMap["en"]!!
    }

    private fun setupUI() {
        binding.voiceText.text = guidanceSteps[0]
        binding.btnContinue.text = "Start"
        binding.btnPause.text = "Finish"
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            if (isGuidanceActive) {
                nextStep()
            } else {
                continueGuidance()
            }
        }

        binding.btnPause.setOnClickListener {
            if (isGuidanceActive) {
                pauseGuidance()
            } else {
                showCompletionDialog()
            }
        }

        // אם תרצה אפשר להוסיף כפתור הקלטה ולחבר כאן (אפשר לעזור בזה)
    }

    private fun continueGuidance() {
        isGuidanceActive = true
        binding.btnContinue.text = "Next"
        binding.btnPause.text = "Pause"
        speakCurrentStep()
    }

    private fun pauseGuidance() {
        isGuidanceActive = false
        binding.btnContinue.text = "Continue"
        binding.btnPause.text = "Finish"
        tts?.stop()
    }

    private fun nextStep() {
        currentStep++
        if (currentStep >= guidanceSteps.size) {
            showCompletionDialog()
            return
        }

        binding.voiceText.text = guidanceSteps[currentStep]
        speakCurrentStep()
    }

    private fun speakCurrentStep() {
        val text = guidanceSteps.getOrNull(currentStep) ?: return
        tts?.language = currentLocale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "guidanceStep")
        binding.voiceText.text = text
    }

    private fun showCompletionDialog() {
        pauseGuidance()

        com.example.myphoneapp.ui.common.CompletionDialog.showVoiceGuidanceCompletion(
            this,
            onAnotherRound = { resetGuidance() },
            onFinish = { finish() }
        )
    }

    private fun resetGuidance() {
        currentStep = 0
        isGuidanceActive = false
        binding.voiceText.text = guidanceSteps[0]
        binding.btnContinue.text = "Continue"
        binding.btnPause.text = "Finish"
    }

    // --- SpeechRecognizer setup ---

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
                val prefix = when (currentLocale.language) {
                    "he" -> "את אמרת:"
                    "ru" -> "Ты сказала:"
                    else -> "You said:"
                }
                binding.voiceText.append("\n$prefix $spokenText")
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLocale.toString())
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(currentLocale)
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
