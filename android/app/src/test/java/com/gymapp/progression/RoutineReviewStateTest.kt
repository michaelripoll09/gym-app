package com.gymapp.progression

import com.gymapp.network.RoutineReviewResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineReviewStateTest {
    @Test fun `maps backend no active plan to an empty review`() {
        assertEquals(RoutineReviewContent.EMPTY, RoutineReviewState(RoutineReviewResponse("NO_ACTIVE_PLAN")).content())
    }
}
