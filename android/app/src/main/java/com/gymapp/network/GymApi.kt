package com.gymapp.network

import com.gymapp.BuildConfig
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
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
@Serializable data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
@Serializable data class PasswordResetRequest(val email: String)
@Serializable data class PasswordResetConfirmationRequest(val token: String, val newPassword: String)
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
@Serializable data class WorkoutPlanResponse(val id: String, val name: String, val days: List<WorkoutPlanDayResponse>, val active: Boolean = false)
@Serializable data class CuratedPlanResponse(val id: String, val name: String, val description: String, val primaryProfile: String, val experienceLevel: String, val goal: String, val days: List<WorkoutPlanDayResponse>)
@Serializable data class GuidedRoutineProposalResponse(val name: String, val explanation: String, val source: String, val days: List<WorkoutPlanDayResponse>)
@Serializable data class SetLogRequest(val exerciseId: String, val repetitions: Int, val loadKg: Double? = null)
@Serializable data class CreateWorkoutSessionRequest(val sets: List<SetLogRequest>, val perceivedExertion: Int? = null, val note: String? = null)
@Serializable data class UpdateWorkoutSessionRequest(val sets: List<SetLogRequest>, val perceivedExertion: Int? = null, val note: String? = null)
@Serializable data class ProgressMilestoneResponse(val exerciseName: String, val type: String, val value: Double, val achievedAt: String)
@Serializable data class CreateWorkoutSessionResponse(val id: String, val milestones: List<ProgressMilestoneResponse> = emptyList())
@Serializable data class SessionSetResponse(val exerciseName: String, val repetitions: Int, val loadKg: Double? = null, val exerciseId: String = "")
@Serializable data class WorkoutSessionResponse(val id: String, val planName: String, val startedAt: String, val sets: List<SessionSetResponse>, val perceivedExertion: Int? = null, val note: String? = null)
@Serializable data class NextWeeklySessionResponse(val planName: String, val dayName: String)
@Serializable data class WeeklyTrainingSummaryResponse(val completedSessions: Int, val scheduledSessions: Int, val adherencePercent: Int, val volumeKg: Double, val nextSession: NextWeeklySessionResponse? = null)
@Serializable data class ExerciseProgressionResponse(val exerciseName: String, val previousRepetitions: Int, val latestRepetitions: Int, val previousLoadKg: Double, val latestLoadKg: Double, val action: String, val explanation: String)
@Serializable data class PersonalRecordResponse(val exerciseName: String, val maximumLoadKg: Double? = null, val maximumLoadAt: String? = null, val maximumRepetitions: Int, val maximumRepetitionsAt: String)
@Serializable data class ProgressAnalysisResponse(val periodDays: Int, val completedSessions: Int, val scheduledSessions: Int, val adherencePercent: Int? = null, val weightChangeKg: Double? = null, val activeGoals: Int, val goalsWithCurrentValue: Int, val recentPersonalRecords: Int, val sufficientData: Boolean, val sources: List<String>)
@Serializable data class RoutineReviewSuggestionResponse(val dayName: String, val exerciseName: String, val action: String, val explanation: String, val sources: List<String>)
@Serializable data class RoutineReviewResponse(val state: String, val activePlanId: String? = null, val activePlanName: String? = null, val suggestions: List<RoutineReviewSuggestionResponse> = emptyList())
@Serializable data class BodyMeasurementRequest(val recordedOn: String, val weightKg: Double, val waistCm: Double? = null, val hipCm: Double? = null, val chestCm: Double? = null)
@Serializable data class BodyMeasurementResponse(val id: String, val recordedOn: String, val weightKg: Double, val waistCm: Double? = null, val hipCm: Double? = null, val chestCm: Double? = null)
@Serializable data class ProgressGoalRequest(val type: String, val targetValue: Double, val targetDate: String? = null, val exerciseName: String? = null)
@Serializable data class ProgressGoalResponse(val id: String, val type: String, val targetValue: Double, val targetDate: String? = null, val status: String, val currentValue: Double? = null, val exerciseName: String? = null, val completedAt: String? = null)
@Serializable data class CalendarDayResponse(val date: String, val completed: Boolean, val scheduled: Boolean, val planName: String? = null, val setCount: Int = 0)

interface GymApi {
    @POST("auth/register") suspend fun register(@Body request: RegisterRequest): AuthResponse
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): AuthResponse
    @POST("auth/password-resets") suspend fun requestPasswordReset(@Body request: PasswordResetRequest)
    @POST("auth/password-resets/confirm") suspend fun confirmPasswordReset(@Body request: PasswordResetConfirmationRequest)
    @DELETE("me") suspend fun deleteAccount(@Header("Authorization") authorization: String)
    @PUT("me/password") suspend fun changePassword(@Header("Authorization") authorization: String, @Body request: ChangePasswordRequest)
    @PUT("me/training-profile") suspend fun saveProfile(@Header("Authorization") authorization: String, @Body request: TrainingProfileRequest)
    @GET("me/training-profile") suspend fun trainingProfile(@Header("Authorization") authorization: String): TrainingProfileResponse
    @GET("exercises") suspend fun exercises(@Query("profile") profile: String): List<ExerciseResponse>
    @GET("curated-plans") suspend fun curatedPlans(@Header("Authorization") authorization: String): List<CuratedPlanResponse>
    @POST("curated-plans/{planId}/adopt") suspend fun adoptCuratedPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String): IdResponse
    @POST("guided-routines/proposal") suspend fun guidedRoutineProposal(@Header("Authorization") authorization: String): GuidedRoutineProposalResponse
    @POST("workout-plans") suspend fun createWorkoutPlan(@Header("Authorization") authorization: String, @Body request: CreateWorkoutPlanRequest): IdResponse
    @PUT("workout-plans/{planId}") suspend fun updateWorkoutPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String, @Body request: CreateWorkoutPlanRequest)
    @GET("workout-plans") suspend fun workoutPlans(@Header("Authorization") authorization: String): List<WorkoutPlanResponse>
    @GET("workout-plans/archived") suspend fun archivedWorkoutPlans(@Header("Authorization") authorization: String): List<WorkoutPlanResponse>
    @PUT("workout-plans/{planId}/archive") suspend fun archiveWorkoutPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String)
    @PUT("workout-plans/{planId}/restore") suspend fun restoreWorkoutPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String)
    @PUT("workout-plans/{planId}/activate") suspend fun activateWorkoutPlan(@Header("Authorization") authorization: String, @Path("planId") planId: String)
    @POST("workout-plans/{planId}/sessions") suspend fun createWorkoutSession(@Header("Authorization") authorization: String, @Path("planId") planId: String, @Body request: CreateWorkoutSessionRequest): CreateWorkoutSessionResponse
    @GET("workout-sessions") suspend fun workoutSessions(@Header("Authorization") authorization: String): List<WorkoutSessionResponse>
    @PUT("workout-sessions/{sessionId}") suspend fun updateWorkoutSession(@Header("Authorization") authorization: String, @Path("sessionId") sessionId: String, @Body request: UpdateWorkoutSessionRequest)
    @DELETE("workout-sessions/{sessionId}") suspend fun deleteWorkoutSession(@Header("Authorization") authorization: String, @Path("sessionId") sessionId: String)
    @GET("training-summary/weekly") suspend fun weeklyTrainingSummary(@Header("Authorization") authorization: String): WeeklyTrainingSummaryResponse
    @GET("training-calendar") suspend fun trainingCalendar(@Header("Authorization") authorization: String, @Query("month") month: String, @Query("zone") zone: String): List<CalendarDayResponse>
    @GET("training-progress/recommendations") suspend fun progressionRecommendations(@Header("Authorization") authorization: String): List<ExerciseProgressionResponse>
    @GET("training-progress/personal-records") suspend fun personalRecords(@Header("Authorization") authorization: String): List<PersonalRecordResponse>
    @GET("training-progress/analysis") suspend fun progressAnalysis(@Header("Authorization") authorization: String): ProgressAnalysisResponse
    @GET("training-progress/routine-review") suspend fun routineReview(@Header("Authorization") authorization: String): RoutineReviewResponse
    @GET("body-measurements") suspend fun bodyMeasurements(@Header("Authorization") authorization: String): List<BodyMeasurementResponse>
    @POST("body-measurements") suspend fun createBodyMeasurement(@Header("Authorization") authorization: String, @Body request: BodyMeasurementRequest): BodyMeasurementResponse
    @PUT("body-measurements/{measurementId}") suspend fun updateBodyMeasurement(@Header("Authorization") authorization: String, @Path("measurementId") measurementId: String, @Body request: BodyMeasurementRequest)
    @DELETE("body-measurements/{measurementId}") suspend fun deleteBodyMeasurement(@Header("Authorization") authorization: String, @Path("measurementId") measurementId: String)
    @GET("progress-goals") suspend fun progressGoals(@Header("Authorization") authorization: String): List<ProgressGoalResponse>
    @POST("progress-goals") suspend fun createProgressGoal(@Header("Authorization") authorization: String, @Body request: ProgressGoalRequest): ProgressGoalResponse
    @PUT("progress-goals/{id}") suspend fun updateProgressGoal(@Header("Authorization") authorization: String, @Path("id") id: String, @Body request: ProgressGoalRequest)
    @PUT("progress-goals/{id}/complete") suspend fun completeProgressGoal(@Header("Authorization") authorization: String, @Path("id") id: String)
    @DELETE("progress-goals/{id}") suspend fun deleteProgressGoal(@Header("Authorization") authorization: String, @Path("id") id: String)
    companion object {
        fun create(): GymApi = Retrofit.Builder().baseUrl("${BuildConfig.API_BASE_URL}/").addConverterFactory(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())).build().create(GymApi::class.java)
    }
}
