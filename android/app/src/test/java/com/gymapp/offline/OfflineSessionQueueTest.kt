package com.gymapp.offline

import com.gymapp.network.CreateWorkoutSessionRequest
import com.gymapp.network.SetLogRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineSessionQueueTest {
    private val pending = PendingSession("local-1", "plan-1", "Fuerza", "2026-08-01T12:00:00Z", CreateWorkoutSessionRequest(listOf(SetLogRequest("exercise-1", 8, 40.0))))

    @Test fun `adds a completed offline session to the pending queue`() {
        assertEquals(listOf(pending), enqueuePendingSession(emptyList(), pending))
    }

    @Test fun `does not enqueue the same local session twice`() {
        assertEquals(listOf(pending), enqueuePendingSession(listOf(pending), pending))
    }

    @Test fun `removes only the session confirmed by the backend`() {
        val second = pending.copy(localId = "local-2")
        assertEquals(listOf(second), removeSyncedSession(listOf(pending, second), pending.localId))
    }

    @Test fun `keeps a pending session after a recoverable synchronization error`() {
        assertTrue(keepPendingAfterFailure(pending) === pending)
    }

    @Test fun `round trips pending sessions through persistent storage format`() {
        assertEquals(listOf(pending), OfflineSessionCodec.decode(OfflineSessionCodec.encode(listOf(pending))))
    }
}
