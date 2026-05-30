package com.gallatinapps.syntaxmp.benchmarks

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

internal data class BenchmarkComparison(
    val sourcePath: Path,
    val rows: List<BenchmarkComparisonRow>,
)

internal data class BenchmarkComparisonRow(
    val status: BenchmarkComparisonStatus,
    val id: String,
    val name: String,
    val currentMedianMs: Double?,
    val baselineMedianMs: Double?,
) {
    val deltaMs: Double? =
        if (currentMedianMs != null && baselineMedianMs != null) {
            currentMedianMs - baselineMedianMs
        } else {
            null
        }

    val deltaPercent: Double? =
        if (deltaMs != null && baselineMedianMs != null && baselineMedianMs != 0.0) {
            (deltaMs / baselineMedianMs) * 100.0
        } else {
            null
        }
}

internal enum class BenchmarkComparisonStatus(
    val displayName: String,
) {
    Compared("Compared"),
    New("New"),
    Missing("Missing"),
}

internal fun loadBenchmarkComparison(
    compareToPath: String?,
    currentResults: List<BenchmarkCaseResult>,
): BenchmarkComparison? {
    val requestedPath = compareToPath?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val sourcePath = resolveComparisonSource(Path.of(requestedPath))
    val baselineRows = when (sourcePath.name) {
        "metadata.json" -> parseMetadataJsonRows(sourcePath)
        else -> parseFullDetailsMarkdownRows(sourcePath)
    }
    require(baselineRows.isNotEmpty()) {
        "No comparable benchmark rows found in $sourcePath"
    }

    val baselineById = baselineRows.associateBy { it.id }
    val currentById = currentResults.associateBy { it.id }
    val comparisonRows = buildList {
        currentResults.forEach { current ->
            val baseline = baselineById[current.id]
            add(
                BenchmarkComparisonRow(
                    status = if (baseline == null) {
                        BenchmarkComparisonStatus.New
                    } else {
                        BenchmarkComparisonStatus.Compared
                    },
                    id = current.id,
                    name = current.name,
                    currentMedianMs = current.samples.medianMs,
                    baselineMedianMs = baseline?.medianMs,
                ),
            )
        }
        baselineRows
            .asSequence()
            .filter { it.id !in currentById }
            .sortedBy { it.id }
            .forEach { baseline ->
                add(
                    BenchmarkComparisonRow(
                        status = BenchmarkComparisonStatus.Missing,
                        id = baseline.id,
                        name = baseline.name,
                        currentMedianMs = null,
                        baselineMedianMs = baseline.medianMs,
                    ),
                )
            }
    }
    return BenchmarkComparison(
        sourcePath = sourcePath,
        rows = comparisonRows,
    )
}

private data class BaselineBenchmarkRow(
    val id: String,
    val name: String,
    val medianMs: Double,
)

private fun resolveComparisonSource(path: Path): Path {
    if (Files.isDirectory(path)) {
        return listOf(
            path.resolve("metadata.json"),
            path.resolve("report.md"),
            path.resolve("full-details.md"),
            path.resolve("syntaxmp-benchmark-report.md"),
        ).firstOrNull(Files::exists)
            ?: error("No metadata.json, report.md, or full-details.md found in $path")
    }

    if (Files.exists(path)) {
        val siblingMetadata = path.parent?.resolve("metadata.json")
        return if (
            path.name in setOf("index.md", "summary.md", "language-matrix.md", "report.md") &&
            siblingMetadata != null &&
            Files.exists(siblingMetadata)
        ) {
            siblingMetadata
        } else {
            path
        }
    }

    error("Comparison source does not exist: $path")
}

private fun parseMetadataJsonRows(path: Path): List<BaselineBenchmarkRow> {
    return parseBenchmarkMetadataJson(path).rows.map { row ->
        BaselineBenchmarkRow(
            id = row.id,
            name = row.name,
            medianMs = row.medianMs,
        )
    }
}

private fun parseFullDetailsMarkdownRows(path: Path): List<BaselineBenchmarkRow> {
    val lines = Files.readAllLines(path)
    val headerIndex = lines.indexOfFirst { line ->
        line.startsWith("| Case ID |") && "Median ms" in line
    }
    if (headerIndex < 0) return emptyList()

    val headerCells = parseMarkdownTableCells(lines[headerIndex])
    val idIndex = headerCells.indexOf("Case ID")
    val nameIndex = headerCells.indexOf("Case")
    val medianIndex = headerCells.indexOf("Median ms")
    if (idIndex < 0 || nameIndex < 0 || medianIndex < 0) return emptyList()

    return lines
        .asSequence()
        .drop(headerIndex + 2)
        .takeWhile { it.startsWith("|") }
        .map(::parseMarkdownTableCells)
        .mapNotNull { cells ->
            val id = cells.getOrNull(idIndex) ?: return@mapNotNull null
            val name = cells.getOrNull(nameIndex) ?: id
            val medianMs = cells.getOrNull(medianIndex)?.replace(",", "")?.toDoubleOrNull()
                ?: return@mapNotNull null
            BaselineBenchmarkRow(
                id = id,
                name = name,
                medianMs = medianMs,
            )
        }
        .toList()
}

private fun parseMarkdownTableCells(line: String): List<String> =
    line.trim()
        .trim('|')
        .split("|")
        .map { cell -> cell.trim().replace("\\|", "|") }
