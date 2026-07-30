package com.gymapp.today

import com.gymapp.network.WorkoutPlanDayResponse
import com.gymapp.network.WorkoutPlanResponse
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class TodayTrainingStateTest {
    @Test fun `selects only plans scheduled for the local day`() {
        val monday = WorkoutPlanResponse("1", "Fuerza", listOf(WorkoutPlanDayResponse("Lunes", emptyList())))
        val tuesday = WorkoutPlanResponse("2", "Cardio", listOf(WorkoutPlanDayResponse("Martes", emptyList())))
        assertEquals(listOf(monday), plansForToday(listOf(monday, tuesday), "Lunes"))
    }
    @Test fun `ignores unknown day names`() { assertEquals(emptyList<WorkoutPlanResponse>(), plansForToday(listOf(WorkoutPlanResponse("1", "Invalida", listOf(WorkoutPlanDayResponse("Nunca", emptyList())))), "Lunes")) }

    @Test fun `maps every local day to its Spanish routine name`() {
        assertEquals(
            listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"),
            DayOfWeek.entries.map(::spanishDayName)
        )
    }

    @Test fun `keeps previously loaded plans after a recoverable error`() {
        val plans = listOf(WorkoutPlanResponse("1", "Fuerza", emptyList()))
        assertEquals(plans, todayLoadError(plans).plans)
    }
}
