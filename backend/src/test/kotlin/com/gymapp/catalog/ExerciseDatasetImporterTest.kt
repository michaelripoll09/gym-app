package com.gymapp.catalog

import com.gymapp.profile.TrainingProfileCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class ExerciseDatasetImporterTest {
    @Test
    fun `reads Spanish instructions and derives profiles from the dataset JSON`() {
        val dataset = createTempFile("exercise-dataset", ".json")
        dataset.writeText(
            """[
              {
                "id": "push-up",
                "name": "Push-up",
                "category": "chest",
                "equipment": "body weight",
                "target": "pectorals",
                "instruction_steps": { "es": ["Mantén el cuerpo recto"] }
              }
            ]""".trimIndent(),
        )

        val candidate = ExerciseDatasetImporter.readCandidates(dataset).single()

        assertEquals("push-up", candidate.sourceId)
        assertEquals(listOf("Mantén el cuerpo recto"), candidate.spanishInstructions)
        assertEquals(
            setOf(TrainingProfileCode.CALISTHENICS, TrainingProfileCode.GENERAL_FITNESS),
            candidate.profiles,
        )
    }

    @Test
    fun `reports duplicate identifiers and excludes records missing Spanish instructions`() {
        val report = ExerciseDatasetImporter.validate(
            listOf(
                ImportCandidate("one", "Sentadilla", listOf("Baja controladamente"), setOf(TrainingProfileCode.POWERLIFTING)),
                ImportCandidate("one", "Sentadilla duplicada", listOf("Baja controladamente"), setOf(TrainingProfileCode.POWERLIFTING)),
                ImportCandidate("two", "Unknown", emptyList(), setOf(TrainingProfileCode.GENERAL_FITNESS)),
                ImportCandidate("three", "Sin perfil", listOf("Haz una repetición"), emptySet()),
            ),
        )

        assertEquals(1, report.published)
        assertEquals(1, report.duplicates)
        assertEquals(1, report.excludedMissingSpanishInstructions)
        assertEquals(1, report.excludedUnmappedProfiles)
    }
}
