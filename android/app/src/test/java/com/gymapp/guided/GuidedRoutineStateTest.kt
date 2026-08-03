package com.gymapp.guided

import com.gymapp.network.ExerciseResponse
import com.gymapp.network.GuidedRoutineProposalResponse
import com.gymapp.network.WorkoutPlanDayResponse
import com.gymapp.network.WorkoutPlanExerciseResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuidedRoutineStateTest {
    @Test fun `confirmation maps a proposal into a new editable workout plan`() {
        val proposal = GuidedRoutineProposalResponse("Rutina guiada", "Explicación", "DETERMINISTIC_FALLBACK", listOf(WorkoutPlanDayResponse("Lunes", listOf(WorkoutPlanExerciseResponse("exercise-1", "Sentadilla", 3, 8, 12, 90)))))

        val request = proposal.toCreateWorkoutPlanRequest()

        assertEquals("Rutina guiada", request.name)
        assertEquals("exercise-1", request.days.single().exercises.single().exerciseId)
        assertEquals(90, request.days.single().exercises.single().restSeconds)
    }

    @Test fun `discard removes the proposal without creating a request`() {
        assertNull(discardGuidedRoutine())
    }

    @Test fun `edits and replaces an exercise before creating the routine`() {
        val proposal = GuidedRoutineProposalResponse("Rutina", "Explicación", "DETERMINISTIC_FALLBACK", listOf(WorkoutPlanDayResponse("Lunes", listOf(WorkoutPlanExerciseResponse("exercise-1", "Sentadilla", 3, 8, 12, 90)))))

        val request = GuidedRoutineDraft.from(proposal)
            .rename("Rutina ajustada")
            .updateExercise(0, 0, 4, 6, 10, 120)
            .replaceExercise(0, 0, ExerciseResponse("exercise-2", "Press", ""))
            .toCreateWorkoutPlanRequest()

        assertEquals("Rutina ajustada", request.name)
        assertEquals("exercise-2", request.days.single().exercises.single().exerciseId)
        assertEquals(4, request.days.single().exercises.single().sets)
        assertEquals(6, request.days.single().exercises.single().minRepetitions)
        assertEquals(120, request.days.single().exercises.single().restSeconds)
    }

    @Test fun `adds and removes days and exercises in the local draft`() {
        val proposal = GuidedRoutineProposalResponse("Rutina", "Explicación", "DETERMINISTIC_FALLBACK", listOf(WorkoutPlanDayResponse("Lunes", listOf(WorkoutPlanExerciseResponse("exercise-1", "Sentadilla", 3, 8, 12, 90)))))

        val draft = GuidedRoutineDraft.from(proposal)
            .addDay("Martes")
            .addExercise(1, ExerciseResponse("exercise-2", "Press", ""))
            .removeExercise(0, 0)
            .removeDay(0)

        assertEquals(listOf("Martes"), draft.days.map { it.name })
        assertEquals(listOf("exercise-2"), draft.days.single().exercises.map { it.exercise.id })
    }
}
