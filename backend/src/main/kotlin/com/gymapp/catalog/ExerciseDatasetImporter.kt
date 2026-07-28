package com.gymapp.catalog

import com.gymapp.profile.TrainingProfileCode
import tools.jackson.databind.ObjectMapper
import java.nio.file.Path

data class ImportCandidate(
    val sourceId: String,
    val name: String,
    val spanishInstructions: List<String>,
    val profiles: Set<TrainingProfileCode>,
    val attribution: String? = null,
)

data class CatalogImportReport(
    val published: Int,
    val duplicates: Int,
    val excludedMissingSpanishInstructions: Int,
    val excludedUnmappedProfiles: Int,
)

object ExerciseDatasetImporter {
    fun readCandidates(datasetFile: Path): List<ImportCandidate> {
        val records = ObjectMapper().readTree(datasetFile.toFile())
        require(records.isArray) { "The exercise dataset must be a JSON array" }

        return records.iterator().asSequence().map { record ->
            val metadata = DatasetExerciseMetadata(
                name = record.path("name").asText(),
                category = record.path("category").asText(),
                equipment = record.path("equipment").asText(),
                target = record.path("target").asText(),
            )
            ImportCandidate(
                sourceId = record.path("id").asText(),
                name = metadata.name,
                attribution = record.path("attribution").asText().ifBlank { null },
                spanishInstructions = record.path("instruction_steps").path("es").iterator().asSequence()
                    .map { instruction -> instruction.asText() }
                    .filter(String::isNotBlank)
                    .toList(),
                profiles = AutomaticProfileMapper.map(metadata),
            )
        }.toList()
    }

    fun validate(candidates: List<ImportCandidate>): CatalogImportReport {
        val seenSourceIds = mutableSetOf<String>()
        var published = 0
        var duplicates = 0
        var missingSpanishInstructions = 0
        var unmappedProfiles = 0

        candidates.forEach { candidate ->
            if (!seenSourceIds.add(candidate.sourceId)) {
                duplicates++
            } else when (
                ExerciseCatalogueService.classify(
                    ExerciseDatasetRecord(candidate.sourceId, candidate.name, candidate.spanishInstructions),
                    candidate.profiles,
                )
            ) {
                ExerciseImportDecision.PUBLISHED -> published++
                ExerciseImportDecision.MISSING_ES_INSTRUCTIONS -> missingSpanishInstructions++
                ExerciseImportDecision.UNMAPPED_PROFILE -> unmappedProfiles++
            }
        }

        return CatalogImportReport(published, duplicates, missingSpanishInstructions, unmappedProfiles)
    }
}
