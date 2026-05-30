package com.gallatinapps.syntaxmp.benchmarks

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.Comparator
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.io.path.writeText

internal data class BenchmarkCaseResult(
    val id: String,
    val group: String,
    val name: String,
    val workloadKind: BenchmarkWorkloadKind,
    val sizeName: String,
    val inputChars: Int,
    val inputLines: Int,
    val spanCount: Int,
    val samples: BenchmarkSamples,
    val allocatedBytes: BenchmarkByteSamples?,
    val checksum: Long,
    val warmupIterations: Int,
    val measuredIterations: Int,
) {
    val charsPerMs: Double =
        if (samples.medianMs <= 0.0) 0.0 else inputChars / samples.medianMs

    val spansPerMs: Double =
        if (samples.medianMs <= 0.0 || spanCount <= 0) 0.0 else spanCount / samples.medianMs
}

internal data class BenchmarkMetadata(
    val startedAt: Instant,
    val cpuFamily: String,
    val os: String,
    val jdk: String,
    val kotlinVersion: String,
    val gradleInvocation: String,
    val powerState: String,
)

internal data class BenchmarkReportPaths(
    val historicalReport: Path,
    val latestReport: Path,
)

internal data class BenchmarkReport(
    val metadata: BenchmarkMetadata,
    val results: List<BenchmarkCaseResult>,
    val sinkChecksum: Long,
    val comparison: BenchmarkComparison? = null,
) {
    fun printToConsole() {
        println(toConsoleMarkdown())
    }

    fun writeTo(reportDir: Path): BenchmarkReportPaths {
        Files.createDirectories(reportDir)
        Files.deleteIfExists(reportDir.resolve("syntaxmp-benchmark-report.md"))
        val historicalDir = reportDir.resolve("runs").resolve(runDirectoryName(reportDir))
        val latestDir = reportDir.resolve("latest")

        val historicalReport = writeReportFiles(historicalDir)
        deleteDirectoryIfExists(latestDir)
        val latestReport = writeReportFiles(latestDir)

        return BenchmarkReportPaths(
            historicalReport = historicalReport,
            latestReport = latestReport,
        )
    }

    private fun writeReportFiles(reportDir: Path): Path {
        Files.createDirectories(reportDir)
        reportDir.resolve("metadata.json").writeText(toMetadataJson())
        return reportDir.resolve("report.md").also { report ->
            report.writeText(toReportMarkdown())
        }
    }

    private fun runDirectoryName(reportDir: Path): String {
        val baseName = metadata.startedAt.toString().replace(':', '-')
        var candidate = baseName
        var suffix = 2
        while (Files.exists(reportDir.resolve("runs").resolve(candidate))) {
            candidate = "$baseName-$suffix"
            suffix++
        }
        return candidate
    }

    private fun deleteDirectoryIfExists(directory: Path) {
        if (!Files.exists(directory)) return
        Files.walk(directory).use { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists)
        }
    }

    private fun toReportMarkdown(): String =
        buildString {
            appendLine("# SyntaxMP Benchmark Report")
            appendLine()
            appendRunSummary()
            appendLine()
            appendLine("## Language Matrix")
            appendLine()
            appendLanguageMatrixTable(languageResults)
            appendLine()
            appendLine("## Diagnostics")
            appendLine()
            appendDiagnosticTable(diagnosticResults)
            comparison?.let { comparison ->
                appendLine()
                appendComparisonSection(comparison)
            }
        }

    private fun toConsoleMarkdown(): String =
        buildString {
            appendLine("# SyntaxMP Benchmark Summary")
            appendLine()
            appendRunSummary()
            appendLine()
            appendLine("Detailed tables are written to `report.md`; machine-readable rows are in `metadata.json`.")
        }

    private val languageResults: List<BenchmarkCaseResult>
        get() = results.filter { it.id.startsWith("languages/representative/") }

    private val diagnosticResults: List<BenchmarkCaseResult>
        get() = results.filterNot { it.id.startsWith("languages/representative/") }

    private fun StringBuilder.appendRunSummary() {
        appendLine("- Started: `${metadata.startedAt}`")
        appendLine("- CPU family: `${metadata.cpuFamily}`")
        appendLine("- OS: `${metadata.os}`")
        appendLine("- JDK: `${metadata.jdk}`")
        appendLine("- Kotlin: `${metadata.kotlinVersion}`")
        appendLine("- Gradle invocation: `${metadata.gradleInvocation}`")
        appendLine("- Power: `${metadata.powerState}`")
        appendLine("- Cases: `${results.size}`")
        appendLine("- Allocation tracking: `${allocationTrackingSummary()}`")
        comparison?.let { comparison ->
            appendLine("- Comparison: `${comparison.sourcePath}`")
        }
        appendLine("- Sink checksum: `${sinkChecksum}`")
    }

    private fun StringBuilder.appendDiagnosticTable(tableResults: List<BenchmarkCaseResult>) {
        appendLine("| Group | Case | Kind | Size | Chars | Lines | Spans | Warmups | Samples | Median ms | P90 ms | Median alloc KiB |")
        appendLine("|---|---|---|---|---:|---:|---:|---:|---:|---:|---:|---:|")
        tableResults.forEach { result ->
            appendLine(
                listOf(
                    result.group,
                    result.name,
                    result.workloadKind.displayName,
                    result.sizeName,
                    result.inputChars.toString(),
                    result.inputLines.toString(),
                    result.spanCount.toString(),
                    result.warmupIterations.toString(),
                    result.measuredIterations.toString(),
                    result.samples.medianMs.formatDouble(),
                    result.samples.p90Ms.formatDouble(),
                    result.allocatedBytes.formatMedianKiB(),
                ).joinToString(prefix = "| ", separator = " | ", postfix = " |") { it.escapeTableCell() },
            )
        }
    }

    private fun StringBuilder.appendLanguageMatrixTable(tableResults: List<BenchmarkCaseResult>) {
        appendLine("| Case | Chars | Lines | Spans | Samples | Median ms | P90 ms | Median alloc KiB |")
        appendLine("|---|---:|---:|---:|---:|---:|---:|---:|")
        tableResults.forEach { result ->
            appendLine(
                listOf(
                    result.name,
                    result.inputChars.toString(),
                    result.inputLines.toString(),
                    result.spanCount.toString(),
                    result.measuredIterations.toString(),
                    result.samples.medianMs.formatDouble(),
                    result.samples.p90Ms.formatDouble(),
                    result.allocatedBytes.formatMedianKiB(),
                ).joinToString(prefix = "| ", separator = " | ", postfix = " |") { it.escapeTableCell() },
            )
        }
    }

    private fun StringBuilder.appendComparisonSection(comparison: BenchmarkComparison) {
        appendLine("## Informational Comparison")
        appendLine()
        appendLine("- Baseline: `${comparison.sourcePath}`")
        appendLine("- Status: `informational only; no drift threshold applied`")
        appendLine()
        appendLine("| Status | Case ID | Case | Current median ms | Baseline median ms | Delta ms | Delta % |")
        appendLine("|---|---|---|---:|---:|---:|---:|")
        comparison.rows.forEach { row ->
            appendLine(
                listOf(
                    row.status.displayName,
                    row.id,
                    row.name,
                    row.currentMedianMs.formatOptionalDouble(),
                    row.baselineMedianMs.formatOptionalDouble(),
                    row.deltaMs.formatOptionalSignedDouble(),
                    row.deltaPercent.formatOptionalSignedPercent(),
                ).joinToString(prefix = "| ", separator = " | ", postfix = " |") { it.escapeTableCell() },
            )
        }
    }

    private fun toMetadataJson(): String =
        buildString {
            appendLine("{")
            appendLine("  \"startedAt\": ${metadata.startedAt.toString().jsonString()},")
            appendLine("  \"cpuFamily\": ${metadata.cpuFamily.jsonString()},")
            appendLine("  \"os\": ${metadata.os.jsonString()},")
            appendLine("  \"jdk\": ${metadata.jdk.jsonString()},")
            appendLine("  \"kotlinVersion\": ${metadata.kotlinVersion.jsonString()},")
            appendLine("  \"gradleInvocation\": ${metadata.gradleInvocation.jsonString()},")
            appendLine("  \"powerState\": ${metadata.powerState.jsonString()},")
            appendLine("  \"allocationTracking\": ${allocationTrackingSummary().jsonString()},")
            appendLine("  \"comparisonSource\": ${comparison?.sourcePath?.toString()?.jsonString() ?: "null"},")
            appendLine("  \"sinkChecksum\": $sinkChecksum,")
            appendLine("  \"results\": [")
            results.forEachIndexed { index, result ->
                appendLine("    {")
                appendLine("      \"id\": ${result.id.jsonString()},")
                appendLine("      \"group\": ${result.group.jsonString()},")
                appendLine("      \"name\": ${result.name.jsonString()},")
                appendLine("      \"kind\": ${result.workloadKind.displayName.jsonString()},")
                appendLine("      \"size\": ${result.sizeName.jsonString()},")
                appendLine("      \"chars\": ${result.inputChars},")
                appendLine("      \"lines\": ${result.inputLines},")
                appendLine("      \"spans\": ${result.spanCount},")
                appendLine("      \"warmups\": ${result.warmupIterations},")
                appendLine("      \"samples\": ${result.measuredIterations},")
                appendLine("      \"medianMs\": ${result.samples.medianMs.formatJsonDouble()},")
                appendLine("      \"p90Ms\": ${result.samples.p90Ms.formatJsonDouble()},")
                appendLine("      \"medianAllocBytes\": ${result.allocatedBytes?.medianBytes ?: "null"},")
                appendLine("      \"checksum\": ${result.checksum}")
                append("    }")
                if (index != results.lastIndex) {
                    appendLine(",")
                } else {
                    appendLine()
                }
            }
            appendLine("  ]")
            appendLine("}")
        }

    private fun String.escapeTableCell(): String =
        replace("|", "\\|")

    private fun allocationTrackingSummary(): String =
        if (results.any { it.allocatedBytes != null }) {
            "enabled via JVM thread allocated bytes"
        } else {
            "unavailable on this JVM"
        }
}

internal fun collectBenchmarkMetadata(): BenchmarkMetadata =
    BenchmarkMetadata(
        startedAt = Instant.now(),
        cpuFamily = System.getProperty("syntaxmp.benchmark.cpu")
            ?: System.getenv("SYNTAXMP_BENCHMARK_CPU")
            ?: System.getenv("PROCESSOR_IDENTIFIER")
            ?: detectMacHardwareValue("Chip")
            ?: System.getProperty("os.arch", "unknown"),
        os = listOfNotNull(
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch"),
        ).joinToString(separator = " "),
        jdk = listOfNotNull(
            System.getProperty("java.vm.name"),
            System.getProperty("java.version"),
            System.getProperty("java.vendor"),
        ).joinToString(separator = " "),
        kotlinVersion = KotlinVersion.CURRENT.toString(),
        gradleInvocation = System.getProperty("syntaxmp.benchmark.gradleInvocation")
            ?: "../gradlew -p . :syntaxmp-benchmarks:runSyntaxMpBenchmarks",
        powerState = System.getProperty("syntaxmp.benchmark.power")
            ?: System.getenv("SYNTAXMP_BENCHMARK_POWER")
            ?: detectMacPowerState()
            ?: "unknown",
    )

private fun Double.formatDouble(): String =
    "%,.3f".format(Locale.US, this)

private fun Double?.formatOptionalDouble(): String =
    this?.formatDouble() ?: "n/a"

private fun Double?.formatOptionalSignedDouble(): String =
    this?.let { value ->
        if (value >= 0.0) {
            "+${value.formatDouble()}"
        } else {
            value.formatDouble()
        }
    } ?: "n/a"

private fun Double?.formatOptionalSignedPercent(): String =
    this?.let { value ->
        val formatted = value.formatDouble()
        if (value >= 0.0) {
            "+$formatted%"
        } else {
            "$formatted%"
        }
    } ?: "n/a"

private fun Double.formatJsonDouble(): String =
    "%.6f".format(Locale.US, this)

private fun BenchmarkByteSamples?.formatMedianKiB(): String =
    this?.medianBytes?.formatKiB() ?: "n/a"

private fun BenchmarkByteSamples?.formatP90KiB(): String =
    this?.p90Bytes?.formatKiB() ?: "n/a"

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

private fun detectMacHardwareValue(label: String): String? {
    if (!System.getProperty("os.name", "").contains("Mac", ignoreCase = true)) {
        return null
    }
    return runCommand("system_profiler", "SPHardwareDataType")
        ?.lineSequence()
        ?.map { it.trim() }
        ?.firstOrNull { it.startsWith("$label:") }
        ?.substringAfter(':')
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

private fun detectMacPowerState(): String? {
    if (!System.getProperty("os.name", "").contains("Mac", ignoreCase = true)) {
        return null
    }
    val firstLine = runCommand("pmset", "-g", "batt")
        ?.lineSequence()
        ?.firstOrNull()
        ?.trim()
        ?: return null
    return firstLine
        .substringAfter("'", missingDelimiterValue = firstLine)
        .substringBefore("'", missingDelimiterValue = firstLine)
        .takeIf { it.isNotBlank() }
}

private fun runCommand(vararg command: String): String? =
    runCatching {
        val process = ProcessBuilder(*command)
            .redirectErrorStream(true)
            .start()
        if (!process.waitFor(2, TimeUnit.SECONDS)) {
            process.destroy()
            return null
        }
        process.inputStream.bufferedReader().readText().takeIf { process.exitValue() == 0 }
    }.getOrNull()
