package com.gymapp.onboarding

enum class TrainingProfile(val label: String) { GENERAL_FITNESS("Fitness general"), BODYBUILDING("Bodybuilding"), POWERLIFTING("Powerlifting"), RUNNING("Running"), CROSSFIT("CrossFit"), CALISTHENICS("Calistenia") }

data class ProfileSelectionState(val primary: TrainingProfile? = null, val secondaryProfiles: Set<TrainingProfile> = emptySet(), val goal: String = "MUSCLE_GAIN", val days: Int = 3, val minutes: Int = 60, val validationMessage: String? = null) {
    fun toggleSecondary(profile: TrainingProfile): ProfileSelectionState = when {
        profile == primary -> copy(validationMessage = "El interés secundario no puede repetir el perfil principal")
        profile in secondaryProfiles -> copy(secondaryProfiles = secondaryProfiles - profile, validationMessage = null)
        secondaryProfiles.size == 2 -> copy(validationMessage = "Puedes elegir hasta dos intereses secundarios")
        else -> copy(secondaryProfiles = secondaryProfiles + profile, validationMessage = null)
    }
}
