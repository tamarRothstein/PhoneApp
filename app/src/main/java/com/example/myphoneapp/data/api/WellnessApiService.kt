package com.example.myphoneapp.data.api

import com.example.myphoneapp.data.model.ServerRequest
import com.example.myphoneapp.data.model.ServerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface WellnessApiService {

    @POST("api/v1/analyze-health")
    suspend fun analyzeHealthData(@Body request: ServerRequest): Response<ServerResponse>

    @POST("api/v1/emergency-alert")
    suspend fun triggerEmergencyAlert(@Body request: ServerRequest): Response<EmergencyResponse>

    @GET("api/v1/user/{userId}/recommendations")
    suspend fun getUserRecommendations(@Path("userId") userId: String): Response<RecommendationsResponse>

    companion object {
        const val BASE_URL = "https://your-server-url.com/" // Replace with your actual server URL
    }
}

data class EmergencyResponse(
    val success: Boolean,
    val emergencyId: String,
    val message: String
)

data class RecommendationsResponse(
    val recommendations: List<com.example.myphoneapp.data.model.RecommendedAction>,
    val personalizedTips: List<String>
)