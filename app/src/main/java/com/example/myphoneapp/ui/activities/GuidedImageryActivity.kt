package com.example.myphoneapp.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.databinding.ActivityGuidedImageryBinding
import java.util.*

class GuidedImageryActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityGuidedImageryBinding
    private var textToSpeech: TextToSpeech? = null
    private var currentSceneIndex = 0
    private var isPlaying = false

    private val scenes = arrayOf(
        "Ocean",
        "Forest",
        "Safe Room"
    )

    private val sceneTexts = arrayOf(
        "Close your eyes and imagine you're sitting on a peaceful beach. Feel the warm sand beneath you, soft and comforting. Listen to the gentle waves washing onto the shore with a rhythmic, soothing sound. The sun is warming your skin gently, not too hot, just perfectly comfortable. Take a deep breath and smell the fresh ocean air, clean and salty. Feel the light breeze on your face as you watch the endless blue horizon. You are completely safe and at peace in this beautiful place.",

        "Picture yourself in a calm forest clearing surrounded by tall, majestic trees. Their leaves rustle softly in the gentle breeze, creating nature's own peaceful symphony. Sunlight filters through the branches above, creating beautiful patterns of light and shadow on the soft grass beneath your feet. You hear birds singing peacefully in the distance, and perhaps the gentle sound of a stream flowing nearby. The air is fresh and clean, filled with the wonderful scent of pine and earth. You feel completely connected to nature and at peace.",

        "Imagine your perfect safe space, a place where you feel completely secure and at peace. This might be a cozy room with soft lighting, comfortable furniture, and everything arranged just the way you like it. Look around and notice what makes this place special to you. Perhaps there are favorite books, soft blankets, or meaningful objects that bring you comfort. Feel the safety and tranquility that surrounds you here. This is your sanctuary, where nothing can disturb your peace of mind."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuidedImageryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeTextToSpeech()
        setupViews()
        setupClickListeners()
        updateSceneDisplay()
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val result = tts.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    binding.pauseButton.text = "TTS not available"
                    binding.pauseButton.isEnabled = false
                } else {
                    // הגדרת פרמטרים לקול מרגיע
                    tts.setSpeechRate(0.8f) // קצב איטי
                    tts.setPitch(0.9f) // טון נמוך יותר

                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            runOnUiThread {
                                binding.pauseButton.text = "Pause"
                                isPlaying = true
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            runOnUiThread {
                                binding.pauseButton.text = "Play Again"
                                isPlaying = false
                                showCompletionDialog()
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            runOnUiThread {
                                binding.pauseButton.text = "Error occurred"
                                isPlaying = false
                            }
                        }
                    })
                }
            }
        } else {
            binding.pauseButton.text = "TTS initialization failed"
            binding.pauseButton.isEnabled = false
        }
    }

    private fun setupViews() {
        binding.sceneDescription.text = "Choose your peaceful scene:"
    }

    private fun setupClickListeners() {
        // כפתור חזרה
        binding.btnBack.setOnClickListener {
            finish()
        }

        // כפתורי בחירת סצנה
        binding.oceanButton.setOnClickListener {
            selectScene(0)
        }

        binding.forestButton.setOnClickListener {
            selectScene(1)
        }

        binding.safeRoomButton.setOnClickListener {
            selectScene(2)
        }

        // כפתורי בקרה
        binding.pauseButton.setOnClickListener {
            togglePlayPause()
        }

        binding.finishButton.setOnClickListener {
            finish()
        }
    }

    private fun selectScene(index: Int) {
        stopSpeaking()
        currentSceneIndex = index
        updateSceneDisplay()
    }

    private fun updateSceneDisplay() {
        binding.currentSceneTitle.text = scenes[currentSceneIndex]
        binding.guidanceText.text = sceneTexts[currentSceneIndex]

        // עדכון בחירת הכפתורים
        resetButtonStyles()
        when (currentSceneIndex) {
            0 -> highlightButton(binding.oceanButton)
            1 -> highlightButton(binding.forestButton)
            2 -> highlightButton(binding.safeRoomButton)
        }
    }

    private fun resetButtonStyles() {
        binding.oceanButton.backgroundTintList = getColorStateList(com.example.myphoneapp.R.color.button_secondary)
        binding.forestButton.backgroundTintList = getColorStateList(com.example.myphoneapp.R.color.button_secondary)
        binding.safeRoomButton.backgroundTintList = getColorStateList(com.example.myphoneapp.R.color.button_secondary)
    }

    private fun highlightButton(button: android.widget.Button) {
        button.backgroundTintList = getColorStateList(com.example.myphoneapp.R.color.primary_turquoise)
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            stopSpeaking()
        } else {
            startSpeaking()
        }
    }

    private fun startSpeaking() {
        val text = sceneTexts[currentSceneIndex]
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "guided_imagery")

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "guided_imagery")
    }

    private fun stopSpeaking() {
        textToSpeech?.stop()
        binding.pauseButton.text = "Play"
        isPlaying = false
    }

    private fun showCompletionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Guided Imagery Complete")
            .setMessage("Wonderful! You've completed the guided imagery session. This visualization practice helps reduce stress, calm the mind, and create a sense of inner peace. You can return to this peaceful place in your mind whenever you need it.")
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Listen Again") { dialog, _ ->
                dialog.dismiss()
                startSpeaking()
            }
            .create()

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    override fun onPause() {
        super.onPause()
        stopSpeaking()
    }
}