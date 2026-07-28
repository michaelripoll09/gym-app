package com.gymapp.catalog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogImportCommandArgumentsTest {
    @Test
    fun `reads the pinned import parameters from command arguments`() {
        val arguments = CatalogImportCommandArguments.from(
            arrayOf("exercises.json", "hasaneyldrm/exercises-dataset", "abc123", "a".repeat(64)),
        )

        assertEquals("exercises.json", arguments.datasetFile.toString())
        assertEquals("hasaneyldrm/exercises-dataset", arguments.source.name)
        assertEquals("abc123", arguments.source.commit)
    }
}
