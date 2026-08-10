package com.gymapp.sessions

import com.gymapp.network.ProgressMilestoneResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionMilestoneStateTest {
    @Test
    fun `formats load and repetitions records for the completion summary`() {
        assertEquals(
            "Carga máxima: 45 kg",
            milestoneValueLabel(ProgressMilestoneResponse("Sentadilla", "LOAD", 45.0, "2026-08-10T00:00:00Z")),
        )
        assertEquals(
            "Máximo de repeticiones: 12",
            milestoneValueLabel(ProgressMilestoneResponse("Dominadas", "REPETITIONS", 12.0, "2026-08-10T00:00:00Z")),
        )
    }

    @Test
    fun `uses a singular or plural completion title`() {
        val milestone = ProgressMilestoneResponse("Sentadilla", "LOAD", 45.0, "2026-08-10T00:00:00Z")
        assertEquals("Nuevo récord personal", milestoneSummaryTitle(listOf(milestone)))
        assertEquals("Nuevos récords personales", milestoneSummaryTitle(listOf(milestone, milestone)))
    }
}
