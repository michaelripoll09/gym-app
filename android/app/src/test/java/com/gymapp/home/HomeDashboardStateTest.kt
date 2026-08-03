package com.gymapp.home

import com.gymapp.network.ProgressGoalResponse
import com.gymapp.network.WorkoutPlanResponse
import com.gymapp.today.TodayTrainingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDashboardStateTest {
    @Test
    fun `explains when the active routine has no workout today`() {
        assertEquals(
            "Tu rutina activa no tiene entrenamiento programado para hoy.",
            dashboardTodayMessage(TodayTrainingState(loading = false, hasActivePlan = true)),
        )
    }

    @Test
    fun `prioritizes an active goal for the dashboard`() {
        val completed = ProgressGoalResponse("done", "BODY_WEIGHT", 70.0, status = "COMPLETED")
        val active = ProgressGoalResponse("active", "EXERCISE_LOAD", 100.0, status = "ACTIVE", exerciseName = "Sentadilla")

        assertEquals(active, dashboardPrimaryGoal(listOf(completed, active)))
    }

    @Test
    fun `keeps cached training visible after a recoverable load error`() {
        val cached = WorkoutPlanResponse("local", "Rutina local", emptyList(), active = true)
        val state = TodayTrainingState(loading = false, plans = listOf(cached), error = "No pudimos cargar tu entrenamiento de hoy")

        assertTrue(dashboardUsesCachedTraining(state))
        assertNull(dashboardTodayMessage(state))
    }
}
