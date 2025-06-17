package com.example.myphoneapp.data.api

import com.example.myphoneapp.data.model.*
import kotlinx.coroutines.delay
import retrofit2.Response

class FakeWellnessApiService : WellnessApiService {

    override suspend fun analyzeHealthData(request: ServerRequest): Response<ServerResponse> {
        delay(1000) // simulate network delay

        val response = ServerResponse(
            emotionalState = EmotionalState(
                state = "alert",
                confidence = 0.9f,
                recommendedAction = "breathing",
                message = "Elevated stress detected"
            ),
            shouldAlert = true,
            alertMessage = "Would you like some help?",
            recommendations = listOf(
                RecommendedAction(
                    type = ActionType.BREATHING,
                    title = "Breathing Together",
                    description = "Guided breathing",
                    duration = 5
                )
            )
        )
        return Response.success(response)
    }

    override suspend fun triggerEmergencyAlert(request: ServerRequest): Response<EmergencyResponse> {
        return Response.success(
            EmergencyResponse(true, "123", "Emergency triggered")
        )
    }

    override suspend fun getUserRecommendations(userId: String): Response<RecommendationsResponse> {
        return Response.success(
            RecommendationsResponse(
                recommendations = listOf(),
                personalizedTips = listOf("Stay hydrated", "Breathe slowly")
            )
        )
    }
}