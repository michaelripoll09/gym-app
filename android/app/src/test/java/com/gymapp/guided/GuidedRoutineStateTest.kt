package com.gymapp.guided

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
}
