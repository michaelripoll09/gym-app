package com.gymapp.profile

enum class ExperienceLevel { BEGINNER, INTERMEDIATE, ADVANCED }

enum class TrainingProfileCode {
    GENERAL_FITNESS,
    BODYBUILDING,
    POWERLIFTING,
    RUNNING,
    CROSSFIT,
    CALISTHENICS,
}

enum class AvailabilityBand { LOW, MEDIUM, HIGH }

data class TrainingProfileRequest(
    val experienceLevel: ExperienceLevel,
    val primaryProfile: TrainingProfileCode,
    val secondaryProfiles: List<TrainingProfileCode>,
    val goal: String,
    val availabilityBand: AvailabilityBand,
    val availableDaysPerWeek: Int,
    val sessionDurationMinutes: Int,
)

object ProfileRules {
    fun validate(request: TrainingProfileRequest) {
        if (request.secondaryProfiles.size > 2) throw ProfileValidationException()
        if (request.secondaryProfiles.contains(request.primaryProfile)) throw ProfileValidationException()
        if (request.secondaryProfiles.distinct().size != request.secondaryProfiles.size) throw ProfileValidationException()
        if (request.availableDaysPerWeek !in 1..7) throw ProfileValidationException()
        if (request.sessionDurationMinutes !in 15..240) throw ProfileValidationException()
        if (request.goal.isBlank()) throw ProfileValidationException()
    }
}

class ProfileValidationException : RuntimeException()
