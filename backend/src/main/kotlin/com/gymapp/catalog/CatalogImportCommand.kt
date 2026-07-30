package com.gymapp.catalog

import com.gymapp.GymAppApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import java.nio.file.Path

data class CatalogImportCommandArguments(
    val datasetFile: Path,
    val source: CatalogSource,
) {
    companion object {
        fun from(args: Array<String>): CatalogImportCommandArguments {
            require(args.size == 4) { "Usage: <dataset-file> <source-name> <source-commit> <source-sha256>" }
            return CatalogImportCommandArguments(Path.of(args[0]), CatalogSource(args[1], args[2], args[3]))
        }
    }
}

fun main(args: Array<String>) {
    val command = CatalogImportCommandArguments.from(args)
    val context = SpringApplicationBuilder(GymAppApplication::class.java)
        .web(WebApplicationType.NONE)
        .run()
    try {
        val report = context.getBean(CatalogImportService::class.java).import(command.datasetFile, command.source)
        println("Catalog import report: published=${report.published}, duplicates=${report.duplicates}, missingSpanishInstructions=${report.excludedMissingSpanishInstructions}, unmappedProfiles=${report.excludedUnmappedProfiles}")
    } finally {
        context.close()
    }
}
