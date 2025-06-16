package com.example.myphoneapp.data.repository

import com.example.myphoneapp.data.api.NetworkModule
import com.example.myphoneapp.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class WellnessRepository {

    private val apiService = NetworkModule.apiService

    suspend fun analyzeHealthData(
        userId: String,
        healthData: HealthData
    ): Flow<Result<ServerResponse>> = flow {
        try {
            val request = ServerRequest(
                userId = userId,
                healthData = healthData,
                deviceInfo = DeviceInfo(
                    deviceId = "android_device_001",
                    deviceType = "Android",
                    appVersion = "1.0.0"
                )
            )

            val response = apiService.analyzeHealthData(request)

            if (response.isSuccessful) {
                response.body()?.let { serverResponse ->
                    emit(Result.success(serverResponse))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(HttpException(response)))
            }
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun triggerEmergencyAlert(
        userId: String,
        healthData: HealthData
    ): Flow<Result<Boolean>> = flow {
        try {
            val request = ServerRequest(
                userId = userId,
                healthData = healthData,
                deviceInfo = DeviceInfo(
                    deviceId = "android_device_001",
                    deviceType = "Android",
                    appVersion = "1.0.0"
                )
            )

            val response = apiService.triggerEmergencyAlert(request)

            if (response.isSuccessful) {
                emit(Result.success(true))
            } else {
                emit(Result.failure(HttpException(response)))
            }
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Mock data for testing when server is not available
    fun getMockHealthData(): HealthData {
        return HealthData(
            heartRate = 72,
            stressLevel = 0.3f,
            steps = 8234,
            sleepHours = 7.5f
        )
    }

    fun getMockServerResponse(isEmergency: Boolean = false): ServerResponse {
        return if (isEmergency) {
            ServerResponse(
                emotionalState = EmotionalState(
                    state = EmotionalStateType.Emergency.value,
                    confidence = 0.9f,
                    recommendedAction = "emergency_contact",
                    message = "Critical stress levels detected. Please seek immediate help."
                ),
                shouldAlert = true,
                alertMessage = "Emergency situation detected. Would you like to contact emergency services?",
                recommendations = listOf(
                    RecommendedAction(
                        type = ActionType.EMERGENCY_CONTACT,
                        title = "Emergency Contact",
                        description = "Contact emergency services immediately"
                    )
                )
            )
        } else {
            ServerResponse(
                emotionalState = EmotionalState(
                    state = EmotionalStateType.Alert.value,
                    confidence = 0.7f,
                    recommendedAction = "relaxation",
                    message = "Elevated stress levels detected. Let's work together to find calm."
                ),
                shouldAlert = true,
                alertMessage = "Hey, what's up? Would you like to try some relaxation together?",
                recommendations = listOf(
                    RecommendedAction(
                        type = ActionType.VOICE_GUIDANCE,
                        title = "Voice Guidance",
                        description = "Let me guide you through this moment",
                        duration = 5
                    ),
                    RecommendedAction(
                        type = ActionType.BREATHING,
                        title = "Breathing Together",
                        description = "Guided breathing exercise",
                        duration = 3
                    ),
                    RecommendedAction(
                        type = ActionType.MEDITATION,
                        title = "Meditation",
                        description = "Brief mindfulness meditation",
                        duration = 10
                    ),
                    RecommendedAction(
                        type = ActionType.COUNTING,
                        title = "Count to 10",
                        description = "Simple counting exercise to center yourself",
                        duration = 1
                    ),
                    RecommendedAction(
                        type = ActionType.HYDRATION,
                        title = "Drink Water",
                        description = "Take a moment to hydrate yourself"
                    )
                )
            )
        }
    }
}