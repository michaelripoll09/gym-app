package com.gymapp.network

import com.gymapp.BuildConfig
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

@Serializable data class RegisterRequest(val email: String, val password: String, val acceptedTermsAt: String)
@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class AuthResponse(val userId: String, val accessToken: String)
@Serializable data class TrainingProfileRequest(val experienceLevel: String, val primaryProfile: String, val secondaryProfiles: List<String>, val goal: String, val availabilityBand: String, val availableDaysPerWeek: Int, val sessionDurationMinutes: Int)
@Serializable data class TrainingProfileResponse(val experienceLevel: String, val primaryProfile: String, val secondaryProfiles: List<String>, val goal: String, val availabilityBand: String, val availableDaysPerWeek: Int, val sessionDurationMinutes: Int)
@Serializable data class ExerciseResponse(val id: String, val name: String, val spanishInstructions: String)
@Serializable data class WorkoutPlanExerciseRequest(val exerciseId: String, val sets: Int, val minRepetitions: Int, val maxRepetitions: Int, val restSeconds: Int)
@Serializable data class WorkoutDayRequest(val name: String, val exercises: List<WorkoutPlanExerciseRequest>)
@Serializable data class CreateWorkoutPlanRequest(val name: String, val days: List<WorkoutDayRequest>)
@Serializable data class IdResponse(val id: String)
@Serializable data class WorkoutPlanExerciseResponse(val exerciseId: String, val name: String, val sets: Int, val minRepetitions: Int, val maxRepetitions: Int, val restSeconds: Int)
@Serializable data class WorkoutPlanDayResponse(val name: String, val exercises: List<WorkoutPlanExerciseResponse>)
@Serializable data class WorkoutPlanResponse(val id: String, val name: String, val days: List<WorkoutPlanDayResponse>)
@Serializable data class CuratedPlanResponse(val id: String, val name: String, val description: String, val primaryProfile: String, val experienceLevel: String, val goal: String, val days: List<WorkoutPlanDayResponse>)
@Serializable data class SetLogRequest(val exerciseId: String, val repetitions: Int, val loadKg: Double? = null)
@Serializable data class CreateWorkoutSessionRequest(val sets: List<SetLogRequest>)
@Serializable data class SessionSetResponse(val exerciseName: String, val repetitions: Int, val loadKg: Double? = null)
@Serializable data class WorkoutSessionResponse(val id: String, val planName: String, val startedAt: String, val sets: List<SessionSetResponse>)

interface GymApi {
    @POST("auth/register") suspend fun register(@Body request: RegisterRequest): AuthResponse
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): AuthResponse
    @PUT("me/training-profile") suspend fun saveProfile(@Header("Authorization") authorization: String, @Body request: TrainingProfileRequest)
    @GET("me/training-profile") suspend fun trainingProfile(@Header("Authorization") authorization: String): TrainingProfileResponse
    @GET("exercises") suspend fun exercises(@Query("profile") profile: String): List<ExerciseResponse>
    @GET("curated-plans") suspend fun curatedPlans(@Header("Authorization") authorization: String): List<CuratedPlanResponse>
    @POST("curated-plans/{planId}/adopt") suspend fun adoptCuratedPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String): IdResponse
    @POST("workout-plans") suspend fun createWorkoutPlan(@Header("Authorization") authorization: String, @Body request: CreateWorkoutPlanRequest): IdResponse
    @PUT("workout-plans/{planId}") suspend fun updateWorkoutPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String, @Body request: CreateWorkoutPlanRequest)
    @GET("workout-plans") suspend fun workoutPlans(@Header("Authorization") authorization: String): List<WorkoutPlanResponse>
    @GET("workout-plans/archived") suspend fun archivedWorkoutPlans(@Header("Authorization") authorization: String): List<WorkoutPlanResponse>
    @PUT("workout-plans/{planId}/archive") suspend fun archiveWorkoutPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String)
    @PUT("workout-plans/{planId}/restore") suspend fun restoreWorkoutPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String)
    @POST("workout-plans/{planId}/sessions") suspend fun createWorkoutSession(@Header("Authorization") authorization: String, @Path("planId") planId: String, @Body request: CreateWorkoutSessionRequest): IdResponse
    @GET("workout-sessions") suspend fun workoutSessions(@Header("Authorization") authorization: String): List<WorkoutSessionResponse>
    companion object {
        fun create(): GymApi = Retrofit.Builder().baseUrl("${BuildConfig.API_BASE_URL}/").addConverterFactory(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())).build().create(GymApi::class.java)
    }
}
