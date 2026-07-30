package com.gymapp.catalog

import com.gymapp.profile.TrainingProfileCode

data class DatasetExerciseMetadata(
    val name: String,
    val category: String,
    val equipment: String,
    val target: String,
)

object AutomaticProfileMapper {
    fun map(exercise: DatasetExerciseMetadata): Set<TrainingProfileCode> {
        val normalizedName = exercise.name.lowercase()
        val normalizedCategory = exercise.category.lowercase()
        val normalizedEquipment = exercise.equipment.lowercase()
        val profiles = mutableSetOf<TrainingProfileCode>()

        if (normalizedEquipment == "body weight") {
            profiles += TrainingProfileCode.CALISTHENICS
        }
        if (normalizedCategory != "cardio" && normalizedEquipment != "body weight") {
            profiles += TrainingProfileCode.BODYBUILDING
        }
        if (normalizedEquipment.contains("barbell")) {
            profiles += TrainingProfileCode.BODYBUILDING
            if (normalizedName.contains("squat") || normalizedName.contains("deadlift") || normalizedName.contains("bench press")) {
                profiles += TrainingProfileCode.POWERLIFTING
            }
        }
        if (normalizedCategory == "cardio") {
            profiles += TrainingProfileCode.CROSSFIT
            if (normalizedName.contains("run") || normalizedEquipment.contains("treadmill")) {
                profiles += TrainingProfileCode.RUNNING
            }
        }
        if (profiles.isNotEmpty()) {
            profiles += TrainingProfileCode.GENERAL_FITNESS
        }
        return profiles
    }
}
