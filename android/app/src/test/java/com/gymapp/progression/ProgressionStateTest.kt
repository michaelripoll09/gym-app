package com.gymapp.progression

import com.gymapp.network.ExerciseProgressionResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionStateTest {
    @Test fun `distinguishes all content states and retains recommendation`() {
        val ready = ProgressionState(items = listOf(ExerciseProgressionResponse("Sentadilla", 8, 10, 20.0, 20.0, "INCREASE", "Subiste repeticiones")))
        assertEquals(ProgressionContent.READY, ready.content()); assertEquals("INCREASE", ready.items.single().action)
        assertEquals(ProgressionContent.LOADING, ProgressionState(loading = true).content()); assertEquals(ProgressionContent.ERROR, ProgressionState(error = "Error").content()); assertEquals(ProgressionContent.EMPTY, ProgressionState().content())
    }
}
