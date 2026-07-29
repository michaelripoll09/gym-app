package com.gymapp.progress

import com.gymapp.network.SessionSetResponse
import com.gymapp.network.WorkoutSessionResponse
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingProgressStateTest {
    private fun session(id: String, startedAt: String, repetitions: Int) = WorkoutSessionResponse(
        id = id,
        planName = "Plan $id",
        startedAt = startedAt,
        sets = listOf(SessionSetResponse("Sentadilla", repetitions), SessionSetResponse("Press", repetitions + 1)),
    )

    @Test
    fun `summarizes only sessions completed in the last seven days`() {
        val now = Instant.parse("2026-07-29T12:00:00Z")
        val state = TrainingProgressState.loaded(
            listOf(
                session("recent", "2026-07-28T12:00:00Z", 10),
                session("old", "2026-07-21T11:59:59Z", 20),
                session("invalid", "not-a-date", 30),
            ),
            now,
        )

        assertEquals(ProgressContent.READY, state.content())
        assertEquals(1, state.summary.completedSessions)
        assertEquals(2, state.summary.registeredSets)
        assertEquals(21, state.summary.totalRepetitions)
        assertEquals(listOf("recent", "old", "invalid"), state.recentSessions.map { it.id })
    }

    @Test
    fun `uses empty and error states without totals`() {
        assertEquals(ProgressContent.EMPTY, TrainingProgressState.loaded(emptyList(), Instant.EPOCH).content())
        assertEquals(ProgressContent.ERROR, TrainingProgressState(error = "Sin conexiÃ³n").content())
    }

    @Test
    fun `counts backend timestamps with a space and fractional seconds`() {
        val state = TrainingProgressState.loaded(
            listOf(session("backend", "2026-07-28 16:41:38.614678", 12)),
            Instant.parse("2026-07-29T20:00:00Z"),
        )

        assertEquals(1, state.summary.completedSessions)
        assertEquals(25, state.summary.totalRepetitions)
    }
}
