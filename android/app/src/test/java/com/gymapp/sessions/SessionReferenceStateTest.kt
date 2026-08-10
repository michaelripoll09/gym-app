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

    @Test fun `applies a reference only to blank fields of its exercise sets`() {
        val reference = ExerciseSessionReferenceResponse("squat", 8, 60.0, "2026-08-10T10:00:00Z")
        val draft = SessionDraftState("plan", "Plan", listOf(
            SessionSetDraft("squat", "Sentadilla", 1),
            SessionSetDraft("squat", "Sentadilla", 2, repetitions = "10", loadKg = "55"),
            SessionSetDraft("squat", "Sentadilla", 3, repetitions = "9"),
            SessionSetDraft("row", "Remo", 1),
        ))

        val applied = draft.applyReference("squat", reference)

        assertEquals(SessionSetDraft("squat", "Sentadilla", 1, repetitions = "8", loadKg = "60.0"), applied.sets[0])
        assertEquals(SessionSetDraft("squat", "Sentadilla", 2, repetitions = "10", loadKg = "55"), applied.sets[1])
        assertEquals(SessionSetDraft("squat", "Sentadilla", 3, repetitions = "9"), applied.sets[2])
        assertEquals(SessionSetDraft("row", "Remo", 1), applied.sets[3])
    }

    @Test fun `does not change a draft when an exercise has no reference`() {
        val draft = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("press", "Press", 1)))

        assertEquals(draft, draft.applyReference("press", null))
    }

    @Test fun `offers application only when a reference can fill a matching set`() {
        val reference = ExerciseSessionReferenceResponse("press", 10, 30.0, "2026-08-10T10:00:00Z")
        val complete = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("press", "Press", 1, repetitions = "10", loadKg = "30")))
        val pending = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("press", "Press", 1)))

        assertEquals(false, complete.canApplyReference("press", reference))
        assertEquals(true, pending.canApplyReference("press", reference))
        assertEquals(false, pending.canApplyReference("row", reference))
    }

    @Test fun `reference loading failure has a recoverable message`() {
        assertEquals(
            "No pudimos cargar tus referencias. Puedes reintentar sin interrumpir la sesión.",
            sessionReferencesLoadError(),
        )
    }
}
