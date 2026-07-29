package com.gymapp.onboarding

import com.gymapp.network.TrainingProfileRequest

enum class TrainingProfile(val label: String) { GENERAL_FITNESS("Fitness general"), BODYBUILDING("Bodybuilding"), POWERLIFTING("Powerlifting"), RUNNING("Running"), CROSSFIT("CrossFit"), CALISTHENICS("Calistenia") }

data class ProfileSelectionState(
    val experienceLevel: String = "BEGINNER",
    val primary: TrainingProfile? = null,
    val secondaryProfiles: Set<TrainingProfile> = emptySet(),
    val goal: String = "MUSCLE_GAIN",
    val availabilityBand: String = "MEDIUM",
    val days: Int = 3,
    val minutes: Int = 60,
    val validationMessage: String? = null,
) {
    fun toggleSecondary(profile: TrainingProfile): ProfileSelectionState = when {
        profile == primary -> copy(validationMessage = "El interés secundario no puede repetir el perfil principal")
        profile in secondaryProfiles -> copy(secondaryProfiles = secondaryProfiles - profile, validationMessage = null)
        secondaryProfiles.size == 2 -> copy(validationMessage = "Puedes elegir hasta dos intereses secundarios")
        else -> copy(secondaryProfiles = secondaryProfiles + profile, validationMessage = null)
    }

    fun validationError(): String? = when {
        primary == null -> "Selecciona una disciplina principal"
        secondaryProfiles.contains(primary) -> "El interés secundario no puede repetir el perfil principal"
        secondaryProfiles.size > 2 -> "Puedes elegir hasta dos intereses secundarios"
        days !in 1..7 -> "Selecciona entre 1 y 7 días"
        minutes !in 15..240 -> "La duración debe estar entre 15 y 240 minutos"
        else -> null
    }

    fun toRequest(): TrainingProfileRequest? = if (validationError() != null) null else TrainingProfileRequest(
        experienceLevel, primary!!.name, secondaryProfiles.map { it.name }.sorted(), goal, availabilityBand, days, minutes,
    )
}
