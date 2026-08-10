package com.gymapp.sessions

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionMutationRefreshStateTest {
    @Test
    fun `refreshes every session dependent view after a mutation`() {
        val refreshed = refreshAfterSessionMutation(
            SessionMutationRefreshState(history = 2, progress = 4, calendar = 6, weeklySummary = 8),
        )

        assertEquals(3, refreshed.history)
        assertEquals(5, refreshed.progress)
        assertEquals(7, refreshed.calendar)
        assertEquals(9, refreshed.weeklySummary)
    }
}
