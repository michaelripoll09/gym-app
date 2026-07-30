package com.gymapp.profile

import com.gymapp.network.TrainingProfileResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRecoveryStateTest {
    @Test
    fun `uses the persisted primary profile when one exists`() {
        val state = resolveProfileRecovery(
            TrainingProfileResponse("INTERMEDIATE", "CALISTHENICS", listOf("RUNNING"), "SKILL", "HIGH", 4, 75),
            null,
        )

        assertEquals(ProfileRecoveryState.Existing("CALISTHENICS"), state)
    }

    @Test
    fun `sends a profileless account to onboarding`() {
        assertEquals(ProfileRecoveryState.NeedsOnboarding, resolveProfileRecovery(null, 404))
    }

    @Test
    fun `marks unauthorized profile recovery for session reset`() {
        assertEquals(ProfileRecoveryState.Unauthorized, resolveProfileRecovery(null, 401))
    }

    @Test
    fun `marks other recovery failures as retryable`() {
        assertEquals(ProfileRecoveryState.RetryableFailure, resolveProfileRecovery(null, 500))
    }
}
