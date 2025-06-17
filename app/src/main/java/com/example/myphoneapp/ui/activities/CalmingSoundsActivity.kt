package com.example.myphoneapp.ui.activities

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.myphoneapp.R

class CalmingSoundsActivity : AppCompatActivity() {

    private var ambientPlayer: MediaPlayer? = null
    private var isPlayingAmbient = false
    private var currentSoundText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calming_sounds)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Find the current sound text view
        currentSoundText = findViewById(R.id.txtCurrentSound)
        currentSoundText?.text = "Choose a relaxing sound"
    }

    private fun setupClickListeners() {
        // Back button (ImageButton)
        findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener {
            onBackPressed()
        }

        // Individual sound buttons (LinearLayouts in MaterialCardView)
        findViewById<LinearLayout>(R.id.btnOceanWaves)?.setOnClickListener {
            playAmbientSound("ocean_waves", "Ocean Waves")
        }

        findViewById<LinearLayout>(R.id.btnRainForest)?.setOnClickListener {
            playAmbientSound("rain_forest", "Rain Forest")
        }

        findViewById<LinearLayout>(R.id.btnGentleRain)?.setOnClickListener {
            playAmbientSound("gentle_rain", "Gentle Rain")
        }

        findViewById<LinearLayout>(R.id.btnMountainStream)?.setOnClickListener {
            playAmbientSound("mountain_stream", "Mountain Stream")
        }

        findViewById<LinearLayout>(R.id.btnWindTrees)?.setOnClickListener {
            playAmbientSound("wind_trees", "Wind in Trees")
        }

        findViewById<LinearLayout>(R.id.btnBirdsSinging)?.setOnClickListener {
            playAmbientSound("birds_singing", "Birds Singing")
        }

        // Control buttons
        findViewById<Button>(R.id.startButton)?.setOnClickListener {
            showAmbientSoundsMenu()
        }

        findViewById<Button>(R.id.doneButton)?.setOnClickListener {
            stopAmbientSound()
            finish()
        }
    }

    private fun showAmbientSoundsMenu() {
        val soundOptions = arrayOf(
            "🌊 Ocean Waves",
            "🌲 Rain Forest",
            "🌧️ Gentle Rain",
            "🏔️ Mountain Stream",
            "🍃 Wind in Trees",
            "🐦 Birds Singing",
            "⏹️ Stop All Sounds"
        )

        AlertDialog.Builder(this)
            .setTitle("Choose Relaxing Sounds")
            .setItems(soundOptions) { dialog, which ->
                when (which) {
                    0 -> playAmbientSound("ocean_waves", "Ocean Waves")
                    1 -> playAmbientSound("rain_forest", "Rain Forest")
                    2 -> playAmbientSound("gentle_rain", "Gentle Rain")
                    3 -> playAmbientSound("mountain_stream", "Mountain Stream")
                    4 -> playAmbientSound("wind_trees", "Wind in Trees")
                    5 -> playAmbientSound("birds_singing", "Birds Singing")
                    6 -> stopAmbientSound()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun playAmbientSound(soundType: String, soundName: String) {
        try {
            stopAmbientSound()

            // Try to get the sound resource
            val soundResource = when (soundType) {
                "ocean_waves" -> getResourceId("ocean_waves")
                "rain_forest" -> getResourceId("rain_forest")
                "gentle_rain" -> getResourceId("gentle_rain")
                "mountain_stream" -> getResourceId("mountain_stream")
                "wind_trees" -> getResourceId("wind_trees")
                "birds_singing" -> getResourceId("birds_singing")
                else -> 0
            }

            if (soundResource != 0) {
                ambientPlayer = MediaPlayer.create(this, soundResource)
                ambientPlayer?.isLooping = true
                ambientPlayer?.start()
                isPlayingAmbient = true

                Toast.makeText(this, "Playing: $soundName", Toast.LENGTH_SHORT).show()
                currentSoundText?.text = "🎵 Playing: $soundName"

                // Update start button text
                findViewById<Button>(R.id.startButton)?.text = "Menu"
            } else {
                Toast.makeText(this, "Sound file not available", Toast.LENGTH_SHORT).show()
                currentSoundText?.text = "Sound file not found"
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error playing sound", Toast.LENGTH_SHORT).show()
            currentSoundText?.text = "Error playing sound"
        }
    }

    private fun getResourceId(soundName: String): Int {
        return try {
            resources.getIdentifier(soundName, "raw", packageName)
        } catch (e: Exception) {
            0
        }
    }

    private fun stopAmbientSound() {
        ambientPlayer?.stop()
        ambientPlayer?.release()
        ambientPlayer = null
        isPlayingAmbient = false
        currentSoundText?.text = "No sound playing"

        // Reset start button text
        findViewById<Button>(R.id.startButton)?.text = "Start"

        Toast.makeText(this, "Sounds stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (isPlayingAmbient) {
            AlertDialog.Builder(this)
                .setTitle("Keep Playing?")
                .setMessage("Sound is currently playing. Do you want to keep it playing in the background?")
                .setPositiveButton("Keep Playing") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Stop & Exit") { _, _ ->
                    stopAmbientSound()
                    super.onBackPressed()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't automatically stop sound - let user choose
    }
}