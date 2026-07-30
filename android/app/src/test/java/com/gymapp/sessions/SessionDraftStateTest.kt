package com.gymapp.sessions

import com.gymapp.network.WorkoutPlanDayResponse
import com.gymapp.network.WorkoutPlanExerciseResponse
import com.gymapp.network.WorkoutPlanResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionDraftStateTest {
    private val plan = WorkoutPlanResponse(
        id = "plan-1",
        name = "Rutina A",
        days = listOf(WorkoutPlanDayResponse("Lunes", listOf(WorkoutPlanExerciseResponse("exercise-1", "Sentadilla", 2, 8, 10, 60))))
    )

    @Test
    fun `creates one entry for every planned set`() {
        val state = SessionDraftState.from(plan)

        assertEquals(2, state.sets.size)
        assertEquals(listOf(1, 2), state.sets.map { it.setNumber })
    }

    @Test
    fun `creates a daily session only with the selected days sets`() {
        val multiDay = plan.copy(days = plan.days + WorkoutPlanDayResponse("Jueves", listOf(WorkoutPlanExerciseResponse("exercise-2", "Remo", 3, 8, 10, 60))))

        val daily = SessionDraftState.from(multiDay, "Jueves")
        val complete = SessionDraftState.from(multiDay)

        assertEquals(listOf("Remo", "Remo", "Remo"), daily.sets.map { it.exerciseName })
        assertEquals(5, complete.sets.size)
    }

    @Test
    fun `rejects empty and non positive repetitions`() {
        val state = SessionDraftState.from(plan)
        val invalid = state.updateRepetitions(0, "0")
        val complete = state.updateRepetitions(0, "10").updateRepetitions(1, "8")

        assertEquals("Registra repeticiones mayores que cero en cada serie", state.validationMessage())
        assertEquals("Registra repeticiones mayores que cero en cada serie", invalid.validationMessage())
        assertEquals(null, complete.validationMessage())
    }

    @Test
    fun `accepts an optional non negative load and rejects invalid load`() {
        val complete = SessionDraftState.from(plan).updateRepetitions(0, "10").updateRepetitions(1, "8")

        assertEquals(null, complete.updateLoadKg(0, "42.5").validationMessage())
        assertEquals("Registra una carga en kg válida o déjala vacía", complete.updateLoadKg(0, "-1").validationMessage())
        assertEquals("Registra una carga en kg válida o déjala vacía", complete.updateLoadKg(0, "mucho").validationMessage())
    }

    @Test
    fun `disables finishing while a session is being saved`() {
        assertEquals(false, canFinishSession(true))
        assertEquals(true, canFinishSession(false))
    }
}
