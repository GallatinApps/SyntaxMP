package com.gallatinapps.syntaxmp.benchmarks

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.Locale
import kotlin.io.path.writeText

private const val MinimumAggregateRuns = 3
private const val RecommendedAggregateRuns = 5
private const val UnstableSpreadThresholdPercent = 30.0

internal data class BenchmarkRunAggregate(
    val generatedAt: Instant,
    val sourceRuns: List<BenchmarkMetadataJsonRun>,
    val rows: List<BenchmarkRunAggregateRow>,
    val warnings: List<String>,
) {
    fun printToConsole() {
        println("# SyntaxMP Benchmark Aggregate")
        println()
        println("- Generated: `$generatedAt`")
        println("- Runs: `${sourceRuns.size}`")
        println("- Rows: `${rows.size}`")
        warnings.forEach { warning ->
            println("- Warning: `$warning`")
        }
    }

    fun writeTo(outputParent: Path): BenchmarkRunAggregatePaths {
        Files.createDirectories(outputParent)
        val outputDir = outputParent.resolve(aggregateDirectoryName(outputParent))
        Files.createDirectories(outputDir)
        val index = outputDir.resolve("index.md")
        val dataJson = outputDir.resolve("aggregate.json")
        index.writeText(toMarkdown())
        dataJson.writeText(toJson())
        return BenchmarkRunAggregatePaths(
            index = index,
            dataJson = dataJson,
        )
    }

    private fun aggregateDirectoryName(outputParent: Path): String {
        val baseName = "${generatedAt.toString().replace(':', '-')}-aggregate"
        var candidate = baseName
        var suffix = 2
        while (Files.exists(outputParent.resolve(candidate))) {
            candidate = "$baseName-$suffix"
            suffix++
        }
        return candidate
    }

    private fun toMarkdown(): String =
        buildString {
            appendLine("# SyntaxMP Benchmark Aggregate")
            appendLine()
            appendLine("- Generated: `$generatedAt`")
            appendLine("- Runs: `${sourceRuns.size}`")
            appendLine("- Minimum runs: `$MinimumAggregateRuns`")
            appendLine("- Recommended runs for stability decisions: `$RecommendedAggregateRuns`")
            appendLine("- Unstable flag: `spread >= ${UnstableSpreadThresholdPercent.formatDouble()}%`")
            appendLine("- Source: `metadata.json`")
            appendLine()
            appendWarnings()
            appendLine("## Input Runs")
            appendLine()
            appendSourceRunTable()
            appendLine()
            appendLine("## Aggregate Rows")
            appendLine()
            appendAggregateRowTable()
        }

    private fun StringBuilder.appendWarnings() {
        if (warnings.isEmpty()) return
        appendLine("## Warnings")
        appendLine()
        warnings.forEach { warning ->
            appendLine("- $warning")
        }
        appendLine()
    }

    private fun StringBuilder.appendSourceRunTable() {
        appendLine("| Started | Power | Cases | Source |")
        appendLine("|---|---|---:|---|")
        sourceRuns.forEach { run ->
            appendLine(
                listOf(
                    run.startedAt,
                    run.powerState,
                    run.rows.size.toString(),
                    run.sourcePath.toString(),
                ).joinToString(prefix = "| ", separator = " | ", postfix = " |") { it.escapeTableCell() },
            )
        }
    }

    private fun StringBuilder.appendAggregateRowTable() {
        appendLine("| Unstable | Case ID | Case | Runs | Median of medians ms | Min median ms | Max median ms | Spread % | Worst p90 ms | Median alloc KiB range |")
        appendLine("|---|---|---|---:|---:|---:|---:|---:|---:|---|")
        rows.forEach { row ->
            appendLine(
                listOf(
                    if (row.unstable) "Yes" else "No",
                    row.id,
                    row.name,
                    row.observedRuns.toString(),
                    row.medianOfMediansMs.formatDouble(),
                    row.minMedianMs.formatDouble(),
                    row.maxMedianMs.formatDouble(),
                    row.spreadPercent.formatDouble(),
                    row.worstP90Ms.formatDouble(),
                    row.medianAllocRangeKiB,
                ).joinToString(prefix = "| ", separator = " | ", postfix = " |") { it.escapeTableCell() },
            )
        }
    }

    private fun toJson(): String =
        buildString {
            appendLine("{")
            appendLine("  \"generatedAt\": ${generatedAt.toString().jsonString()},")
            appendLine("  \"runCount\": ${sourceRuns.size},")
            appendLine("  \"minimumRuns\": $MinimumAggregateRuns,")
            appendLine("  \"recommendedRuns\": $RecommendedAggregateRuns,")
            appendLine("  \"unstableSpreadThresholdPercent\": ${UnstableSpreadThresholdPercent.formatJsonDouble()},")
            appendLine("  \"warnings\": [")
            warnings.forEachIndexed { index, warning ->
                append("    ${warning.jsonString()}")
                appendLine(if (index == warnings.lastIndex) "" else ",")
            }
            appendLine("  ],")
            appendLine("  \"sources\": [")
            sourceRuns.forEachIndexed { index, run ->
                appendLine("    {")
                appendLine("      \"startedAt\": ${run.startedAt.jsonString()},")
                appendLine("      \"powerState\": ${run.powerState.jsonString()},")
                appendLine("      \"cases\": ${run.rows.size},")
                appendLine("      \"sourcePath\": ${run.sourcePath.toString().jsonString()}")
                append("    }")
                appendLine(if (index == sourceRuns.lastIndex) "" else ",")
            }
            appendLine("  ],")
            appendLine("  \"rows\": [")
            rows.forEachIndexed { index, row ->
                appendLine("    {")
                appendLine("      \"id\": ${row.id.jsonString()},")
                appendLine("      \"name\": ${row.name.jsonString()},")
                appendLine("      \"observedRuns\": ${row.observedRuns},")
                appendLine("      \"medianOfMediansMs\": ${row.medianOfMediansMs.formatJsonDouble()},")
                appendLine("      \"minMedianMs\": ${row.minMedianMs.formatJsonDouble()},")
                appendLine("      \"maxMedianMs\": ${row.maxMedianMs.formatJsonDouble()},")
                appendLine("      \"spreadPercent\": ${row.spreadPercent.formatJsonDouble()},")
                appendLine("      \"worstP90Ms\": ${row.worstP90Ms.formatJsonDouble()},")
                appendLine("      \"minMedianAllocBytes\": ${row.minMedianAllocBytes ?: "null"},")
                appendLine("      \"maxMedianAllocBytes\": ${row.maxMedianAllocBytes ?: "null"},")
                appendLine("      \"unstable\": ${row.unstable}")
                append("    }")
                appendLine(if (index == rows.lastIndex) "" else ",")
            }
            appendLine("  ]")
            appendLine("}")
        }
}

internal data class BenchmarkRunAggregateRow(
    val id: String,
    val name: String,
    val observedRuns: Int,
    val medianOfMediansMs: Double,
    val minMedianMs: Double,
    val maxMedianMs: Double,
    val spreadPercent: Double,
    val worstP90Ms: Double,
    val minMedianAllocBytes: Long?,
    val maxMedianAllocBytes: Long?,
) {
    val unstable: Boolean = spreadPercent >= UnstableSpreadThresholdPercent

    val medianAllocRangeKiB: String =
        if (minMedianAllocBytes != null && maxMedianAllocBytes != null) {
            "${minMedianAllocBytes.formatKiB()}-${maxMedianAllocBytes.formatKiB()}"
        } else {
            "n/a"
        }
}

internal data class BenchmarkRunAggregatePaths(
    val index: Path,
    val dataJson: Path,
)

internal fun buildBenchmarkRunAggregate(
    sourcePaths: List<Path>,
    generatedAt: Instant = Instant.now(),
): BenchmarkRunAggregate {
    require(sourcePaths.size >= MinimumAggregateRuns) {
        "At least $MinimumAggregateRuns metadata runs are required; got ${sourcePaths.size}"
    }

    val sourceRuns = sourcePaths
        .map(::parseBenchmarkMetadataJson)
        .sortedBy { it.startedAt }
    val warnings = buildList {
        if (sourceRuns.size < RecommendedAggregateRuns) {
            add(
                "Only ${sourceRuns.size} runs were supplied; use at least " +
                    "$RecommendedAggregateRuns runs for stability decisions.",
            )
        }
    }
    val rows = sourceRuns
        .flatMap { run -> run.rows.map { row -> row.id to row } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
        .map { (id, observations) -> observations.toAggregateRow(id) }
        .sortedWith(
            compareByDescending<BenchmarkRunAggregateRow> { it.medianOfMediansMs }
                .thenBy { it.id },
        )

    return BenchmarkRunAggregate(
        generatedAt = generatedAt,
        sourceRuns = sourceRuns,
        rows = rows,
        warnings = warnings,
    )
}

private fun List<BenchmarkMetadataJsonRow>.toAggregateRow(id: String): BenchmarkRunAggregateRow {
    val medianValues = map { it.medianMs }
    val medianOfMediansMs = medianValues.median()
    val minMedianMs = medianValues.minOrNull() ?: 0.0
    val maxMedianMs = medianValues.maxOrNull() ?: 0.0
    val spreadPercent = if (medianOfMediansMs == 0.0) {
        0.0
    } else {
        ((maxMedianMs - minMedianMs) / medianOfMediansMs) * 100.0
    }
    val allocationValues = mapNotNull { it.medianAllocBytes }
    return BenchmarkRunAggregateRow(
        id = id,
        name = firstOrNull()?.name ?: id,
        observedRuns = size,
        medianOfMediansMs = medianOfMediansMs,
        minMedianMs = minMedianMs,
        maxMedianMs = maxMedianMs,
        spreadPercent = spreadPercent,
        worstP90Ms = maxOf { it.p90Ms },
        minMedianAllocBytes = allocationValues.minOrNull(),
        maxMedianAllocBytes = allocationValues.maxOrNull(),
    )
}

private fun List<Double>.median(): Double {
    require(isNotEmpty()) { "Cannot calculate median of an empty list" }
    val sorted = sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) {
        sorted[middle]
    } else {
        (sorted[middle - 1] + sorted[middle]) / 2.0
    }
}

private fun String.escapeTableCell(): String =
    replace("|", "\\|")

private fun Double.formatDouble(): String =
    "%,.3f".format(Locale.US, this)

private fun Double.formatJsonDouble(): String =
    "%.6f".format(Locale.US, this)

private fun Long.formatKiB(): String =
    "%,.1f".format(Locale.US, this / 1024.0)

private fun String.jsonString(): String =
    buildString {
        append('"')
        this@jsonString.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
