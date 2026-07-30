package com.gymapp

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingResultTest {
    @Test
    fun `reports failure when saving the onboarding profile is rejected`() = runBlocking {
        val saved = completeOnboarding { error("HTTP 401") }

        assertEquals(false, saved)
    }
}
