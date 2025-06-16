package com.example.myphoneapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myphoneapp.data.model.ServerResponse
import com.example.myphoneapp.data.repository.WellnessRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = WellnessRepository()

    private val _serverResponse = MutableLiveData<ServerResponse>()
    val serverResponse: LiveData<ServerResponse> = _serverResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun startHealthMonitoring(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // For demonstration, we'll use mock data
                // In a real app, this would continuously monitor health data
                val mockHealthData = repository.getMockHealthData()

                // Simulate server analysis - using mock response for now
                val mockResponse = repository.getMockServerResponse(isEmergency = false)
                _serverResponse.value = mockResponse

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun triggerEmergencyAlert(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val mockHealthData = repository.getMockHealthData()

                repository.triggerEmergencyAlert(userId, mockHealthData).collect { result ->
                    result.onSuccess {
                        // Emergency alert sent successfully
                    }.onFailure { exception ->
                        _error.value = exception.message
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send emergency alert"
            } finally {
                _isLoading.value = false
            }
        }
    }
}