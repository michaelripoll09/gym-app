package com.gymapp.progress

import com.gymapp.network.SessionSetResponse
import com.gymapp.network.WorkoutSessionResponse
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingProgressStateTest {
    @Test
    fun `summarizes latest and maximum load per exercise ignoring unloaded sets`() {
        val sessions = listOf(
            WorkoutSessionResponse("old", "A", "2026-07-28T10:00:00Z", listOf(SessionSetResponse("Sentadilla", 8, 50.0), SessionSetResponse("Press", 8))),
            WorkoutSessionResponse("new", "B", "2026-07-29T10:00:00Z", listOf(SessionSetResponse("Sentadilla", 8, 45.0))),
        )

        assertEquals(listOf(ExerciseLoadProgress("Sentadilla", 45.0, 50.0)), exerciseLoadProgress(sessions))
    }

    @Test
    fun `orders exercises by their latest loaded session then name`() {
        val sessions = listOf(
            WorkoutSessionResponse("input-first", "A", "2026-07-27T10:00:00Z", listOf(SessionSetResponse("Press", 8, 25.0))),
            WorkoutSessionResponse("middle", "B", "2026-07-28T10:00:00Z", listOf(SessionSetResponse("Zancada", 8, 40.0))),
            WorkoutSessionResponse("newest", "C", "2026-07-30T10:00:00Z", listOf(SessionSetResponse("Press", 8, 30.0), SessionSetResponse("Sentadilla", 8, 50.0))),
        )

        assertEquals(listOf("Press", "Sentadilla", "Zancada"), exerciseLoadProgress(sessions).map { it.exerciseName })
    }

    @Test
    fun `returns no exercise load progress when every set has no load`() {
        val sessions = listOf(
            WorkoutSessionResponse("bodyweight", "A", "2026-07-30T10:00:00Z", listOf(SessionSetResponse("Dominada", 8))),
        )

        assertEquals(emptyList<ExerciseLoadProgress>(), exerciseLoadProgress(sessions))
    }

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
        assertEquals(ProgressContent.LOADING, TrainingProgressState(loading = true).content())
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
