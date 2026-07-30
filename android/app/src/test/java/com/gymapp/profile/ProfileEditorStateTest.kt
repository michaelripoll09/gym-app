package com.gymapp.profile

import com.gymapp.network.TrainingProfileResponse
import com.gymapp.onboarding.ProfileSelectionState
import com.gymapp.onboarding.TrainingProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileEditorStateTest {
    private val storedProfile = TrainingProfileResponse(
        experienceLevel = "ADVANCED",
        primaryProfile = "CALISTHENICS",
        secondaryProfiles = listOf("RUNNING"),
        goal = "SKILL",
        availabilityBand = "HIGH",
        availableDaysPerWeek = 5,
        sessionDurationMinutes = 90,
    )

    @Test
    fun `loads every persisted value into an editable selection`() {
        val state = resolveProfileEditor(storedProfile, null)

        assertEquals(
            ProfileEditorState.Editing(
                ProfileSelectionState(
                    experienceLevel = "ADVANCED",
                    primary = TrainingProfile.CALISTHENICS,
                    secondaryProfiles = setOf(TrainingProfile.RUNNING),
                    goal = "SKILL",
                    availabilityBand = "HIGH",
                    days = 5,
                    minutes = 90,
                ),
            ),
            state,
        )
    }

    @Test
    fun `keeps an error recoverable when the profile cannot be loaded`() {
        assertEquals(ProfileEditorState.RetryableFailure, resolveProfileEditor(null, 500))
    }

    @Test
    fun `uses the newly saved primary discipline for the catalog`() {
        assertEquals("RUNNING", profileForUpdatedCatalog("RUNNING"))
    }
}
