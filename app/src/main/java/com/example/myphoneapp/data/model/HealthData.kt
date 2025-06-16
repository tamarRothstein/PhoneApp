package com.example.myphoneapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthData(
    val heartRate: Int,
    val stressLevel: Float,
    val steps: Int,
    val sleepHours: Float,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class EmotionalState(
    val state: String, // "calm", "alert", "emergency"
    val confidence: Float,
    val recommendedAction: String,
    val message: String? = null
) : Parcelable

@Parcelize
data class ServerRequest(
    val userId: String,
    val healthData: HealthData,
    val deviceInfo: DeviceInfo
) : Parcelable

@Parcelize
data class ServerResponse(
    val emotionalState: EmotionalState,
    val shouldAlert: Boolean,
    val alertMessage: String? = null,
    val recommendations: List<RecommendedAction>
) : Parcelable

@Parcelize
data class DeviceInfo(
    val deviceId: String,
    val deviceType: String,
    val appVersion: String
) : Parcelable

@Parcelize
data class RecommendedAction(
    val type: ActionType,
    val title: String,
    val description: String,
    val duration: Int? = null // in minutes
) : Parcelable

enum class ActionType {
    BREATHING,
    MEDITATION,
    COUNTING,
    HYDRATION,
    VOICE_GUIDANCE,
    EMERGENCY_CONTACT
}

sealed class EmotionalStateType(val value: String) {
    object Calm : EmotionalStateType("calm")
    object Alert : EmotionalStateType("alert")
    object Emergency : EmotionalStateType("emergency")
    object Stressed : EmotionalStateType("stressed")
    object Balanced : EmotionalStateType("balanced")
}

data class UserProfile(
    val userId: String,
    val name: String,
    val age: Int,
    val emergencyContact: String? = null,
    val preferences: UserPreferences
)

data class UserPreferences(
    val enableNotifications: Boolean = true,
    val voiceFeedbackEnabled: Boolean = true,
    val emergencyAutoTrigger: Boolean = false,
    val preferredActions: List<ActionType> = emptyList()
)