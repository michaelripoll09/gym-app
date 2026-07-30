package com.gymapp.catalog

import com.gymapp.profile.TrainingProfileCode

data class ExerciseDatasetRecord(
    val sourceId: String,
    val name: String,
    val spanishInstructions: List<String>,
)

enum class ExerciseImportDecision { PUBLISHED, MISSING_ES_INSTRUCTIONS, UNMAPPED_PROFILE }

object ExerciseCatalogueService {
    fun classify(record: ExerciseDatasetRecord, profiles: Set<TrainingProfileCode>): ExerciseImportDecision = when {
        record.spanishInstructions.isEmpty() -> ExerciseImportDecision.MISSING_ES_INSTRUCTIONS
        profiles.isEmpty() -> ExerciseImportDecision.UNMAPPED_PROFILE
        else -> ExerciseImportDecision.PUBLISHED
    }
}
