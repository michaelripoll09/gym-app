package com.gymapp.routines

import com.gymapp.network.ExerciseResponse
import com.gymapp.network.WorkoutPlanDayResponse
import com.gymapp.network.WorkoutPlanExerciseResponse
import com.gymapp.network.WorkoutPlanResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineDraftStateTest {
    @Test
    fun `loads an existing plan into the routine editor draft`() {
        val draft = RoutineDraftState.from(WorkoutPlanResponse("plan-1", "Fuerza", listOf(WorkoutPlanDayResponse("Lunes", listOf(WorkoutPlanExerciseResponse("exercise-1", "Sentadilla", 4, 8, 8, 90))))))

        assertEquals("Fuerza", draft.name)
        assertEquals(setOf("Lunes"), draft.scheduledDays)
        assertEquals(4, draft.exercises.single().sets)
    }
    @Test
    fun `requires a name and at least one exercise before saving`() {
        val empty = RoutineDraftState()
        val named = empty.copy(name = "Rutina inicial").toggleDay("Lunes")
        val complete = named.addExercise(ExerciseResponse("exercise-1", "Sentadilla", "Mantén la espalda recta"))

        assertEquals("Escribe un nombre para la rutina", empty.validationMessage())
        assertEquals("Añade al menos un ejercicio", named.validationMessage())
        assertEquals(null, complete.validationMessage())
    }

    @Test
    fun `rejects non positive series repetitions and rest`() {
        val draft = RoutineDraftState().copy(name = "Fuerza").toggleDay("Martes").addExercise(ExerciseResponse("exercise-1", "Dominadas", "Controla el descenso"))

        val invalid = draft.updateExercise("exercise-1", sets = 0, repetitions = 8, restSeconds = 60)

        assertEquals("Completa series, repeticiones y descanso con valores mayores que cero", invalid.validationMessage())
    }

    @Test
    fun `requires at least one scheduled day`() {
        val draft = RoutineDraftState(name = "Movilidad").addExercise(ExerciseResponse("exercise-1", "Plancha", "Activa el abdomen"))

        val scheduled = draft.toggleDay("Lunes")

        assertEquals("Selecciona al menos un día", draft.validationMessage())
        assertEquals(null, scheduled.validationMessage())
    }

    @Test
    fun `uses different lazy list keys for selected and catalog exercises`() {
        assertEquals(false, routineListKey("selected", "exercise-1") == routineListKey("catalog", "exercise-1"))
    }
}
