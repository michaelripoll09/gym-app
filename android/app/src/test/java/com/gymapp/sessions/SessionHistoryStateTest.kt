package com.gymapp.sessions

import com.gymapp.network.SessionSetResponse
import com.gymapp.network.WorkoutSessionResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionHistoryStateTest {
    private val session = WorkoutSessionResponse(
        id = "session-1",
        planName = "Fuerza base",
        startedAt = "2026-07-28T10:00:00Z",
        sets = listOf(SessionSetResponse("Sentadilla", 8))
    )

    @Test
    fun `shows an empty state only after a successful empty load`() {
        assertEquals(HistoryContent.EMPTY, SessionHistoryState().content())
        assertEquals(HistoryContent.LOADING, SessionHistoryState(loading = true).content())
    }

    @Test
    fun `exposes a recoverable error instead of hiding it`() {
        val state = SessionHistoryState(error = "Sin conexiÃ³n")

        assertEquals(HistoryContent.ERROR, state.content())
        assertEquals("Sin conexiÃ³n", state.error)
    }

    @Test
    fun `selects a loaded session for its detail`() {
        val selected = SessionHistoryState(sessions = listOf(session)).select(session)

        assertEquals(HistoryContent.DETAIL, selected.content())
        assertEquals("Fuerza base", selected.selected?.planName)
        assertEquals(8, selected.selected?.sets?.single()?.repetitions)
    }
}
