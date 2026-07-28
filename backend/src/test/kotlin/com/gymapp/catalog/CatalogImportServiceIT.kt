package com.gymapp.catalog

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.nio.file.Files
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

@SpringBootTest
class CatalogImportServiceIT(
    @Autowired private val service: CatalogImportService,
    @Autowired private val jdbc: JdbcTemplate,
) {
    @AfterEach
    fun clearCatalogue() {
        jdbc.update("delete from exercise_training_profiles where exercise_id in (select id from exercises where source_name = 'fixture-source')")
        jdbc.update("delete from exercises where source_name = 'fixture-source'")
    }

    @Test
    fun `imports published exercise with source traceability and pending editorial review`() {
        val dataset = createTempFile("catalog-import", ".json")
        dataset.writeText(
            """[{"id":"push-up","name":"Push-up","category":"chest","equipment":"body weight","target":"pectorals","attribution":"Source credit","instruction_steps":{"es":["Mantén el cuerpo recto"]}}]""",
        )
        val source = CatalogSource(
            name = "fixture-source",
            commit = "abc123",
            expectedSha256 = Files.newInputStream(dataset).use { stream -> CatalogueFileHash.sha256(stream) },
        )

        val report = service.import(dataset, source)

        assertEquals(1, report.published)
        assertEquals(1, jdbc.queryForObject("select count(*) from exercises where source_name = 'fixture-source' and published = true", Int::class.java))
        assertEquals("PENDING_EDITORIAL_REVIEW", jdbc.queryForObject("select review_status from exercises where source_name = 'fixture-source'", String::class.java))
        assertEquals(2, jdbc.queryForObject("select count(*) from exercise_training_profiles where exercise_id in (select id from exercises where source_name = 'fixture-source')", Int::class.java))
    }
}
