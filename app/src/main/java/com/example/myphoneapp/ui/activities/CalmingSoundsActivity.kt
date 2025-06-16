package com.example.myphoneapp.ui.activities

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.R
import com.example.myphoneapp.databinding.ActivityCalmingSoundsBinding

class CalmingSoundsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalmingSoundsBinding
    private var mediaPlayer: MediaPlayer? = null
    private var currentSoundIndex = 0
    private var isPlaying = false

    // רשימת הצלילים
    private val soundTitles = arrayOf(
        "Forest Sounds",
        "Ocean Waves",
        "Rain Drops",
        "Breathing Music",
        "Gong Sounds",
        "White Noise"
    )

    private val soundDescriptions = arrayOf(
        "Rustling leaves and bird songs",
        "Gentle ocean waves on shore",
        "Soft rainfall on leaves",
        "Calm breathing meditation music",
        "Peaceful gong meditation",
        "Soothing white noise"
    )

    // קובצי האודיו - אם לא קיימים, נשתמש בהנחיה איך להוסיף
    private val soundResources = arrayOf(
        R.raw.forest_sounds,    // forest_sounds.mp3
        R.raw.ocean_waves,      // ocean_waves.mp3
        R.raw.rain_drops,       // rain_drops.mp3
        R.raw.breathing_music,  // breathing_music.mp3
        R.raw.gong_sounds,      // gong_sounds.mp3
        R.raw.white_noise       // white_noise.mp3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalmingSoundsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupClickListeners()
        updateCurrentSoundDisplay()
    }

    private fun setupViews() {
        binding.statusText.text = "Tap to play"
    }

    private fun setupClickListeners() {
        // כפתור חזרה
        binding.backButton.setOnClickListener {
            finish()
        }

        // כפתורי בקרה
        binding.playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        binding.previousSoundButton.setOnClickListener {
            previousSound()
        }

        binding.nextSoundButton.setOnClickListener {
            nextSound()
        }

        // כפתורי בחירת צלילים
        binding.forestSoundsButton.setOnClickListener {
            selectSound(0)
        }

        binding.oceanSoundsButton.setOnClickListener {
            selectSound(1)
        }

        binding.rainSoundsButton.setOnClickListener {
            selectSound(2)
        }

        binding.breathingMusicButton.setOnClickListener {
            selectSound(3)
        }

        binding.gongSoundsButton.setOnClickListener {
            selectSound(4)
        }

        binding.whiteNoiseButton.setOnClickListener {
            selectSound(5)
        }
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            pauseSound()
        } else {
            playSound()
        }
    }

    private fun playSound() {
        try {
            // עצירת נגן קיים
            stopSound()

            // יצירת נגן חדש עם הקובץ הנוכחי
            mediaPlayer = MediaPlayer.create(this, soundResources[currentSoundIndex])

            mediaPlayer?.let { player ->
                player.isLooping = true // השמעה בלופ
                player.setOnCompletionListener {
                    stopSound()
                }

                player.start()
                isPlaying = true
                binding.playPauseButton.text = "⏸️"
                binding.statusText.text = "Playing ${soundTitles[currentSoundIndex]}"
            }

        } catch (e: Exception) {
            // אם אין קובץ אודיו - הצגת הודעה
            binding.statusText.text = "Audio file not found. Please add ${soundTitles[currentSoundIndex].lowercase().replace(" ", "_")}.mp3 to res/raw/"
            // או דמיה של השמעה
            simulatePlayback()
        }
    }

    private fun pauseSound() {
        try {
            mediaPlayer?.pause()
            isPlaying = false
            binding.playPauseButton.text = "▶️"
            binding.statusText.text = "Paused"
        } catch (e: Exception) {
            stopSound()
        }
    }

    private fun stopSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            // Ignore errors during cleanup
        } finally {
            mediaPlayer = null
            isPlaying = false
            binding.playPauseButton.text = "▶️"
            binding.statusText.text = "Tap to play"
        }
    }

    private fun simulatePlayback() {
        // דמיה של השמעה כשאין קובצי אודיו
        isPlaying = true
        binding.playPauseButton.text = "⏸️"
        binding.statusText.text = "Simulating ${soundTitles[currentSoundIndex]}"

        // עצירה אחרי 30 שניות
        binding.root.postDelayed({
            if (isPlaying) {
                stopSound()
            }
        }, 30000)
    }

    private fun selectSound(index: Int) {
        if (currentSoundIndex != index) {
            stopSound()
            currentSoundIndex = index
            updateCurrentSoundDisplay()
        }
    }

    private fun previousSound() {
        stopSound()
        currentSoundIndex = if (currentSoundIndex > 0) currentSoundIndex - 1 else soundTitles.size - 1
        updateCurrentSoundDisplay()
    }

    private fun nextSound() {
        stopSound()
        currentSoundIndex = if (currentSoundIndex < soundTitles.size - 1) currentSoundIndex + 1 else 0
        updateCurrentSoundDisplay()
    }

    private fun updateCurrentSoundDisplay() {
        binding.currentSoundTitle.text = soundTitles[currentSoundIndex]
        binding.currentSoundDescription.text = soundDescriptions[currentSoundIndex]
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSound()
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pauseSound()
        }
    }
}