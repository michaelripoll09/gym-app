package com.gymapp.sessions

import com.gymapp.network.ExerciseProgressionResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionRecommendationStateTest {
    private val recommendation = ExerciseProgressionResponse(
        exerciseName = "Sentadilla",
        previousRepetitions = 8,
        latestRepetitions = 10,
        previousLoadKg = 20.0,
        latestLoadKg = 20.0,
        action = "INCREASE",
        explanation = "Completaste más repeticiones con la misma carga.",
    )

    @Test fun `shows only the matching exercise recommendation`() {
        assertEquals(recommendation, sessionRecommendationFor("Sentadilla", listOf(recommendation)))
        assertNull(sessionRecommendationFor("Remo", listOf(recommendation)))
    }

    @Test fun `reports insufficient history when no recommendation is available`() {
        assertEquals(SessionRecommendationContent.INSUFFICIENT_HISTORY, SessionRecommendationState().content())
    }

    @Test fun `reports a recoverable recommendation loading error`() {
        assertEquals(SessionRecommendationContent.ERROR, SessionRecommendationState(error = sessionRecommendationsLoadError()).content())
        assertEquals("No pudimos cargar tus recomendaciones. Puedes reintentar sin interrumpir la sesión.", sessionRecommendationsLoadError())
    }

    @Test fun `loading recommendations never overwrites manually entered values`() {
        val draft = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("squat", "Sentadilla", 1, repetitions = "9", loadKg = "55")))

        assertEquals(draft, draft.withRecommendations(listOf(recommendation)))
    }

    @Test fun `offers an explicit application only for a valid recommendation on its exercise`() {
        val draft = SessionDraftState("plan", "Plan", listOf(
            SessionSetDraft("squat", "Sentadilla", 1),
            SessionSetDraft("row", "Remo", 1),
        ))

        assertEquals(true, draft.canApplyRecommendation("squat", recommendation))
        assertEquals(false, draft.canApplyRecommendation("row", recommendation))
        assertEquals(false, draft.canApplyRecommendation("squat", recommendation.copy(latestRepetitions = 0)))
    }

    @Test fun `cancelling a recommendation review leaves manual values unchanged`() {
        val draft = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("squat", "Sentadilla", 1, repetitions = "9", loadKg = "55")))

        val review = requireNotNull(draft.reviewRecommendation("squat", recommendation))

        assertEquals(SessionRecommendationPreview("20.0", "10", recommendation.explanation), review.preview())
        assertEquals(null, review.cancel())
        assertEquals("9", draft.sets.single().repetitions)
        assertEquals("55", draft.sets.single().loadKg)
    }

    @Test fun `confirming applies latest recommendation values only to the chosen exercises sets`() {
        val draft = SessionDraftState("plan", "Plan", listOf(
            SessionSetDraft("squat", "Sentadilla", 1, repetitions = "9", loadKg = "55"),
            SessionSetDraft("squat", "Sentadilla", 2),
            SessionSetDraft("row", "Remo", 1, repetitions = "12", loadKg = "40"),
        ))

        val applied = draft.applyRecommendation("squat", recommendation)

        assertEquals(listOf("10", "10", "12"), applied.sets.map { it.repetitions })
        assertEquals(listOf("20.0", "20.0", "40"), applied.sets.map { it.loadKg })
    }

    @Test fun `applied values remain manually editable without another application`() {
        val draft = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("squat", "Sentadilla", 1)))

        val edited = draft.applyRecommendation("squat", recommendation)
            .updateRepetitions(0, "11")
            .updateLoadKg(0, "22.5")

        assertEquals("11", edited.sets.single().repetitions)
        assertEquals("22.5", edited.sets.single().loadKg)
    }

    @Test fun `offers undo only after a confirmed recommendation for that exercise`() {
        val draft = SessionDraftState("plan", "Plan", listOf(
            SessionSetDraft("squat", "Sentadilla", 1),
            SessionSetDraft("row", "Remo", 1),
        ))

        val applied = draft.applyRecommendation("squat", recommendation)

        assertEquals(true, applied.canUndoRecommendation("squat"))
        assertEquals(false, applied.canUndoRecommendation("row"))
        assertEquals(false, draft.canUndoRecommendation("squat"))
    }

    @Test fun `undo restores the exact prior values only for the applied exercise`() {
        val draft = SessionDraftState("plan", "Plan", listOf(
            SessionSetDraft("squat", "Sentadilla", 1, repetitions = "9", loadKg = "55"),
            SessionSetDraft("squat", "Sentadilla", 2, repetitions = "7", loadKg = "50"),
            SessionSetDraft("row", "Remo", 1, repetitions = "12", loadKg = "40"),
        ))

        val undone = draft.applyRecommendation("squat", recommendation).undoRecommendation("squat")

        assertEquals(draft.sets, undone.sets)
        assertEquals(false, undone.canUndoRecommendation("squat"))
    }

    @Test fun `manual repetition or load edits invalidate undo for the applied exercise only`() {
        val draft = SessionDraftState("plan", "Plan", listOf(
            SessionSetDraft("squat", "Sentadilla", 1),
            SessionSetDraft("row", "Remo", 1),
        )).applyRecommendation("squat", recommendation)

        assertEquals(false, draft.updateRepetitions(0, "11").canUndoRecommendation("squat"))
        assertEquals(false, draft.updateLoadKg(0, "22.5").canUndoRecommendation("squat"))
    }

    @Test fun `a new application replaces the pending undo snapshot for that exercise`() {
        val first = recommendation.copy(latestRepetitions = 10, latestLoadKg = 20.0)
        val second = recommendation.copy(latestRepetitions = 12, latestLoadKg = 25.0)
        val draft = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("squat", "Sentadilla", 1, repetitions = "8", loadKg = "15")))

        val undone = draft.applyRecommendation("squat", first).applyRecommendation("squat", second).undoRecommendation("squat")

        assertEquals("10", undone.sets.single().repetitions)
        assertEquals("20.0", undone.sets.single().loadKg)
    }

    @Test fun `clearing the session draft removes every pending undo`() {
        val draft = SessionDraftState("plan", "Plan", listOf(SessionSetDraft("squat", "Sentadilla", 1)))
            .applyRecommendation("squat", recommendation)

        assertEquals(false, draft.clearRecommendationUndos().canUndoRecommendation("squat"))
    }
}
