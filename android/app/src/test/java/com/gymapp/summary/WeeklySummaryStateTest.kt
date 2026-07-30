package com.gymapp.summary

import com.gymapp.network.NextWeeklySessionResponse
import com.gymapp.network.WeeklyTrainingSummaryResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklySummaryStateTest {
    @Test
    fun `shows summary metrics and next session when data is available`() {
        val state = WeeklySummaryState(summary = WeeklyTrainingSummaryResponse(2, 3, 66, 1250.0, NextWeeklySessionResponse("Fuerza", "Viernes")))

        assertEquals(WeeklySummaryContent.READY, state.content())
        assertEquals("Fuerza", state.summary?.nextSession?.planName)
        assertEquals(1250.0, state.summary?.volumeKg)
    }

    @Test
    fun `distinguishes loading error and empty weekly states`() {
        assertEquals(WeeklySummaryContent.LOADING, WeeklySummaryState(loading = true).content())
        assertEquals(WeeklySummaryContent.ERROR, WeeklySummaryState(error = "Sin red").content())
        assertEquals(WeeklySummaryContent.EMPTY, WeeklySummaryState(summary = WeeklyTrainingSummaryResponse(0, 0, 0, 0.0, null)).content())
    }
}
