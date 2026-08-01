package com.gymapp.offline

import android.content.Context
import com.gymapp.network.WorkoutPlanResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OfflineTrainingStore(context: Context) {
    private val preferences = context.getSharedPreferences("gym_app_offline_training", Context.MODE_PRIVATE)

    fun cachePlans(plans: List<WorkoutPlanResponse>) { preferences.edit().putString("active_plans", json.encodeToString(plans)).apply() }
    fun cachedPlans(): List<WorkoutPlanResponse> = decode("active_plans")
    fun pendingSessions(): List<PendingSession> = OfflineSessionCodec.decode(preferences.getString("pending_sessions", "[]") ?: "[]")
    fun savePendingSessions(sessions: List<PendingSession>) { preferences.edit().putString("pending_sessions", OfflineSessionCodec.encode(sessions)).apply() }

    private inline fun <reified T> decode(key: String): List<T> = runCatching { json.decodeFromString<List<T>>(preferences.getString(key, "[]") ?: "[]") }.getOrDefault(emptyList())
    private companion object { val json = Json { ignoreUnknownKeys = true } }
}
