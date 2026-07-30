package com.gymapp.profile

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ProfileRulesTest {
    @Test
    fun `accepts a beginner calisthenics profile`() {
        assertDoesNotThrow {
            ProfileRules.validate(
                TrainingProfileRequest(
                    experienceLevel = ExperienceLevel.BEGINNER,
                    primaryProfile = TrainingProfileCode.CALISTHENICS,
                    secondaryProfiles = listOf(TrainingProfileCode.RUNNING),
                    goal = "MUSCLE_GAIN",
                    availabilityBand = AvailabilityBand.MEDIUM,
                    availableDaysPerWeek = 3,
                    sessionDurationMinutes = 60,
                ),
            )
        }
    }

    @Test
    fun `rejects more than two secondary profiles`() {
        assertThrows(ProfileValidationException::class.java) {
            ProfileRules.validate(validRequest(secondaryProfiles = listOf(TrainingProfileCode.RUNNING, TrainingProfileCode.CROSSFIT, TrainingProfileCode.POWERLIFTING)))
        }
    }

    @Test
    fun `rejects a secondary profile equal to the primary profile`() {
        assertThrows(ProfileValidationException::class.java) {
            ProfileRules.validate(validRequest(secondaryProfiles = listOf(TrainingProfileCode.CALISTHENICS)))
        }
    }

    @Test
    fun `rejects availability outside the approved limits`() {
        assertThrows(ProfileValidationException::class.java) { ProfileRules.validate(validRequest(availableDaysPerWeek = 0)) }
        assertThrows(ProfileValidationException::class.java) { ProfileRules.validate(validRequest(sessionDurationMinutes = 241)) }
    }

    private fun validRequest(
        secondaryProfiles: List<TrainingProfileCode> = emptyList(),
        availableDaysPerWeek: Int = 3,
        sessionDurationMinutes: Int = 60,
    ) = TrainingProfileRequest(
        experienceLevel = ExperienceLevel.BEGINNER,
        primaryProfile = TrainingProfileCode.CALISTHENICS,
        secondaryProfiles = secondaryProfiles,
        goal = "MUSCLE_GAIN",
        availabilityBand = AvailabilityBand.MEDIUM,
        availableDaysPerWeek = availableDaysPerWeek,
        sessionDurationMinutes = sessionDurationMinutes,
    )
}
