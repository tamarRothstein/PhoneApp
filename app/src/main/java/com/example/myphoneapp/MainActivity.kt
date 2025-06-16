package com.example.myphoneapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.myphoneapp.databinding.ActivityMainBinding
import com.example.myphoneapp.ui.main.MainViewModel
import com.example.myphoneapp.ui.alert.AlertActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeViewModel()

        // Start monitoring health data
        viewModel.startHealthMonitoring("user_123") // Replace with actual user ID
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun observeViewModel() {
        viewModel.serverResponse.observe(this) { response ->
            if (response.shouldAlert) {
                when (response.emotionalState.state) {
                    "emergency" -> {
                        // Launch emergency activity immediately
                        // For now, we'll show the alert with emergency message
                        launchAlertActivity(response.alertMessage ?: "Emergency situation detected!")
                    }
                    "alert", "stressed" -> {
                        // Show relaxation options via AlertActivity
                        response.alertMessage?.let { message ->
                            launchAlertActivity(message)
                        }
                    }
                }
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            // Handle error - could show a snackbar or toast
            // For now, we'll use mock data when server is unavailable
        }
    }

    private fun launchAlertActivity(message: String) {
        val intent = Intent(this, AlertActivity::class.java)
        intent.putExtra("ALERT_MESSAGE", message)
        startActivity(intent)
    }
}