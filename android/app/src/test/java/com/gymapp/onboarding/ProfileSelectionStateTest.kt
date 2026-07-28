package com.gymapp.onboarding

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
}
