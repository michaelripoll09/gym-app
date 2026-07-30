package com.gymapp.curated

import com.gymapp.network.CuratedPlanResponse
import com.gymapp.network.WorkoutPlanDayResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedPlansStateTest {
    private val plan = CuratedPlanResponse(
        "starter-calisthenics-beginner-muscle-gain", "Base de calistenia", "Plan inicial", "CALISTHENICS", "BEGINNER", "MUSCLE_GAIN", listOf(WorkoutPlanDayResponse("Lunes", emptyList()))
    )

    @Test
    fun `shows loaded curated plans`() {
        assertEquals(CuratedPlansContent.READY, CuratedPlansState(plans = listOf(plan)).content())
    }

    @Test
    fun `keeps loading empty and error states distinct`() {
        assertEquals(CuratedPlansContent.LOADING, CuratedPlansState(loading = true).content())
        assertEquals(CuratedPlansContent.EMPTY, CuratedPlansState().content())
        assertEquals(CuratedPlansContent.ERROR, CuratedPlansState(error = "Sin conexión").content())
    }
}
