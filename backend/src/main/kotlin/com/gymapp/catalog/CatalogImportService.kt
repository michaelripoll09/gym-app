package com.gymapp.catalog

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.UUID

data class CatalogSource(
    val name: String,
    val commit: String,
    val expectedSha256: String,
)

object CatalogueFileHash {
    fun sha256(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        while (true) {
            val bytesRead = input.read(buffer)
            if (bytesRead < 0) break
            digest.update(buffer, 0, bytesRead)
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }
}

@Service
class CatalogImportService(private val jdbc: JdbcTemplate) {
    @Transactional
    fun import(datasetFile: Path, source: CatalogSource): CatalogImportReport {
        val actualHash = Files.newInputStream(datasetFile).use(CatalogueFileHash::sha256)
        require(actualHash.equals(source.expectedSha256, ignoreCase = true)) { "Dataset SHA-256 does not match the pinned source" }

        val candidates = ExerciseDatasetImporter.readCandidates(datasetFile)
        val report = ExerciseDatasetImporter.validate(candidates)
        candidates.distinctBy { it.sourceId }
            .filter { candidate -> ExerciseCatalogueService.classify(ExerciseDatasetRecord(candidate.sourceId, candidate.name, candidate.spanishInstructions), candidate.profiles) == ExerciseImportDecision.PUBLISHED }
            .forEach { candidate -> persist(candidate, source) }
        return report
    }

    private fun persist(candidate: ImportCandidate, source: CatalogSource) {
        val id = UUID.nameUUIDFromBytes("${source.name}:${candidate.sourceId}".toByteArray())
        jdbc.update(
            """insert into exercises (id, source_name, source_external_id, source_commit, source_file_sha256, name, spanish_instructions, attribution, published, review_status)
               values (?, ?, ?, ?, ?, ?, ?, ?, true, 'PENDING_EDITORIAL_REVIEW')
               on conflict (source_name, source_external_id) do update set
                 source_commit = excluded.source_commit,
                 source_file_sha256 = excluded.source_file_sha256,
                 name = excluded.name,
                 spanish_instructions = excluded.spanish_instructions,
                 attribution = excluded.attribution,
                 published = excluded.published,
                 review_status = excluded.review_status""",
            id, source.name, candidate.sourceId, source.commit, source.expectedSha256.lowercase(), candidate.name,
            candidate.spanishInstructions.joinToString("\n"), candidate.attribution,
        )
        jdbc.update("delete from exercise_training_profiles where exercise_id = ?", id)
        candidate.profiles.forEach { profile ->
            jdbc.update("insert into exercise_training_profiles (exercise_id, profile_code) values (?, ?)", id, profile.name)
        }
    }
}
