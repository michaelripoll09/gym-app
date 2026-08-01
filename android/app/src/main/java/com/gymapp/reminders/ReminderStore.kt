package com.gymapp.reminders

import android.content.Context
import com.gymapp.network.WorkoutPlanResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ReminderStore(context: Context) {
    private val preferences = context.getSharedPreferences("gym_app_reminders", Context.MODE_PRIVATE)

    fun readSettings() = ReminderSettings(
        enabled = preferences.getBoolean("enabled", false),
        hour = preferences.getInt("hour", 18),
        minute = preferences.getInt("minute", 0)
    )

    fun saveSettings(settings: ReminderSettings) {
        preferences.edit().putBoolean("enabled", settings.enabled).putInt("hour", settings.hour).putInt("minute", settings.minute).apply()
    }

    fun savePlans(plans: List<WorkoutPlanResponse>) { preferences.edit().putString("plans", json.encodeToString(plans)).apply() }
    fun clear() { preferences.edit().clear().apply() }

    fun readPlans(): List<WorkoutPlanResponse> = runCatching {
        json.decodeFromString<List<WorkoutPlanResponse>>(preferences.getString("plans", "[]") ?: "[]")
    }.getOrDefault(emptyList())

    private companion object { val json = Json { ignoreUnknownKeys = true } }
}
