package com.example.myphoneapp.data.mock

import com.example.myphoneapp.data.model.*
import kotlin.random.Random

object MockDataGenerator {

    val mockUsers = listOf(
        UserProfile(
            userId = "user_001",
            name = "יוסי כהן",
            age = 28,
            emergencyContact = "054-1234567",
            preferences = UserPreferences(
                enableNotifications = true,
                voiceFeedbackEnabled = true,
                emergencyAutoTrigger = false,
                preferredActions = listOf(ActionType.BREATHING, ActionType.MEDITATION)
            )
        )
    )

    fun generateRandomHealthData(): HealthData {
        return HealthData(
            heartRate = Random.nextInt(60, 120),
            stressLevel = Random.nextFloat() * 10f,
            steps = Random.nextInt(1000, 15000),
            sleepHours = Random.nextFloat() * 4f + 4f
        )
    }

    fun generateDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = "DEVICE_001",
            deviceType = "Samsung Galaxy",
            appVersion = "1.0.0"
        )
    }

    fun generateEmotionalState(): EmotionalState {
        return EmotionalState(
            state = "calm",
            confidence = 0.85f,
            recommendedAction = "המשך לנוח ותשמור על מצב הרוגע",
            message = "אתה במצב רוגע מצוין!"
        )
    }

    fun generateRecommendedActions(): List<RecommendedAction> {
        return listOf(
            RecommendedAction(
                type = ActionType.BREATHING,
                title = "תרגיל נשימה עמוק",
                description = "נשימות איטיות ועמוקות למשך 5 דקות",
                duration = 5
            ),
            RecommendedAction(
                type = ActionType.MEDITATION,
                title = "מדיטציה מונחית",
                description = "מדיטציה קצרה להירגעות",
                duration = 10
            )
        )
    }

    fun generateHistoricalData(days: Int = 7): List<HealthData> {
        val data = mutableListOf<HealthData>()
        val currentTime = System.currentTimeMillis()

        for (i in 0 until days) {
            val dayOffset = i * 24 * 60 * 60 * 1000L
            data.add(
                HealthData(
                    heartRate = Random.nextInt(65, 110),
                    stressLevel = Random.nextFloat() * 8f + 1f,
                    steps = Random.nextInt(3000, 12000),
                    sleepHours = Random.nextFloat() * 3f + 5f,
                    timestamp = currentTime - dayOffset
                )
            )
        }
        return data
    }
}