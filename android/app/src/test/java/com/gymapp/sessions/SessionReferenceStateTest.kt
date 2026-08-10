package com.gymapp.sessions

import com.gymapp.network.ExerciseSessionReferenceResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionReferenceStateTest {
    @Test fun `editing a draft does not require or overwrite a reference`() {
        val draft = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("exercise", "Ejercicio", 1)))
        assertEquals("25", draft.updateLoadKg(0, "25").sets.single().loadKg)
        assertEquals("10", draft.updateRepetitions(0, "10").sets.single().repetitions)
    }

    @Test fun `selects only the reference for the current exercise`() {
        val references = listOf(
            ExerciseSessionReferenceResponse("squat", 8, 60.0, "2026-08-10T10:00:00Z"),
            ExerciseSessionReferenceResponse("row", 12, null, "2026-08-11T10:00:00Z"),
        )

        assertEquals("row", sessionReferenceFor("row", references)?.exerciseId)
        assertNull(sessionReferenceFor("press", references))
    }

    @Test fun `reference loading failure has a recoverable message`() {
        assertEquals(
            "No pudimos cargar tus referencias. Puedes reintentar sin interrumpir la sesión.",
            sessionReferencesLoadError(),
        )
    }
}
