package com.gymapp.onboarding

import com.gymapp.network.TrainingProfileRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileSelectionStateTest {
    @Test
    fun `does not add the primary profile as a secondary interest`() {
        val state = ProfileSelectionState(primary = TrainingProfile.CALISTHENICS)

        val next = state.toggleSecondary(TrainingProfile.CALISTHENICS)

        assertEquals(emptySet<TrainingProfile>(), next.secondaryProfiles)
        assertEquals("El interés secundario no puede repetir el perfil principal", next.validationMessage)
    }

    @Test
    fun `builds the selected complete profile request`() {
        val state = ProfileSelectionState(
            experienceLevel = "ADVANCED",
            primary = TrainingProfile.CALISTHENICS,
            secondaryProfiles = setOf(TrainingProfile.RUNNING),
            goal = "SKILL",
            availabilityBand = "HIGH",
            days = 5,
            minutes = 90,
        )

        assertEquals(
            TrainingProfileRequest("ADVANCED", "CALISTHENICS", listOf("RUNNING"), "SKILL", "HIGH", 5, 90),
            state.toRequest(),
        )
    }

    @Test
    fun `rejects a missing primary profile and invalid availability`() {
        val state = ProfileSelectionState(days = 0, minutes = 241)

        assertEquals("Selecciona una disciplina principal", state.validationError())
        assertEquals(null, state.toRequest())
    }
}
