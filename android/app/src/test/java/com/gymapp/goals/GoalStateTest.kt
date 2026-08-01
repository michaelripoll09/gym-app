package com.gymapp.goals

import com.gymapp.network.ProgressGoalResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalStateTest {
    @Test fun `calculates remaining distance from the current body weight`() {
        val goal = ProgressGoalResponse("one", "BODY_WEIGHT", 70.0, null, "ACTIVE", 75.0, null, null)
        assertEquals(5.0, goalProgress(goal).remainingValue, 0.0)
        assertEquals("En progreso", goalProgress(goal).label)
    }

    @Test fun `rejects an invalid optional target date before saving`() {
        assertEquals(
            "Indica una fecha valida en formato AAAA-MM-DD.",
            goalInputError("BODY_WEIGHT", "70", "not-a-date", "")
        )
    }
}
