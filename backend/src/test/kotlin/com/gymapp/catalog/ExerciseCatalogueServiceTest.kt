package com.gymapp.catalog

import com.gymapp.profile.TrainingProfileCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExerciseCatalogueServiceTest {
    @Test
    fun `publishes a mapped exercise with Spanish instructions`() {
        val result = ExerciseCatalogueService.classify(
            ExerciseDatasetRecord("push-up", "Flexión", listOf("Mantén el cuerpo recto")),
            setOf(TrainingProfileCode.CALISTHENICS),
        )

        assertEquals(ExerciseImportDecision.PUBLISHED, result)
    }

    @Test
    fun `excludes exercises without Spanish instructions or profile mappings`() {
        assertEquals(ExerciseImportDecision.MISSING_ES_INSTRUCTIONS, ExerciseCatalogueService.classify(ExerciseDatasetRecord("x", "X", emptyList()), setOf(TrainingProfileCode.RUNNING)))
        assertEquals(ExerciseImportDecision.UNMAPPED_PROFILE, ExerciseCatalogueService.classify(ExerciseDatasetRecord("x", "X", listOf("Instrucción")), emptySet()))
    }
}
