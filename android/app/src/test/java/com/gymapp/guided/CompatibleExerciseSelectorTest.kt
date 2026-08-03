package com.gymapp.guided

import com.gymapp.network.ExerciseResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class CompatibleExerciseSelectorTest {
    @Test fun `filters compatible catalog locally and reports no matches`() {
        val catalog = listOf(ExerciseResponse("1", "Sentadilla", ""), ExerciseResponse("2", "Press banca", ""))

        assertEquals(listOf("1"), filterCompatibleExercises(catalog, "SENT").map { it.id })
        assertEquals(emptyList<String>(), filterCompatibleExercises(catalog, "running").map { it.id })
    }
}
