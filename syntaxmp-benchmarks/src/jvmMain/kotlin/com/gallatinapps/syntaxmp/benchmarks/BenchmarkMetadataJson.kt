package com.gallatinapps.syntaxmp.benchmarks

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

internal data class BenchmarkMetadataJsonRun(
    val sourcePath: Path,
    val startedAt: String,
    val cpuFamily: String,
    val os: String,
    val jdk: String,
    val kotlinVersion: String,
    val gradleInvocation: String,
    val powerState: String,
    val allocationTracking: String,
    val rows: List<BenchmarkMetadataJsonRow>,
)

internal data class BenchmarkMetadataJsonRow(
    val id: String,
    val group: String,
    val name: String,
    val kind: String,
    val size: String,
    val chars: Int,
    val lines: Int,
    val spans: Int,
    val warmups: Int,
    val samples: Int,
    val medianMs: Double,
    val p90Ms: Double,
    val medianAllocBytes: Long?,
    val checksum: Long,
)

internal fun resolveBenchmarkMetadataJson(path: Path): Path {
    val sourcePath = path.toAbsolutePath().normalize()
    if (Files.isDirectory(sourcePath)) {
        val metadataPath = sourcePath.resolve("metadata.json")
        require(Files.exists(metadataPath)) {
            "No metadata.json found in $sourcePath"
        }
        return metadataPath
    }

    if (Files.exists(sourcePath)) {
        val siblingMetadata = sourcePath.parent?.resolve("metadata.json")
        return if (
            sourcePath.name in setOf("index.md", "summary.md", "language-matrix.md", "report.md") &&
            siblingMetadata != null &&
            Files.exists(siblingMetadata)
        ) {
            siblingMetadata
        } else {
            sourcePath
        }
    }

    error("Benchmark metadata source does not exist: $sourcePath")
}

/**
 * Reads a `metadata.json` produced by this module's own writer (see [BenchmarkReport]). This is a
 * deliberately minimal reader for a dev-only harness, not a general JSON parser: it assumes the
 * pretty-printed, one-property-per-line shape the writer emits (top-level scalar properties plus a
 * single `"results"` array of flat objects). Top-level key order does not matter; compact/minified
 * JSON is not supported. Kept dependency-free on purpose (this module stays dependency-light).
 */
internal fun parseBenchmarkMetadataJson(path: Path): BenchmarkMetadataJsonRun {
    val metadataPath = resolveBenchmarkMetadataJson(path)
    require(metadataPath.name == "metadata.json") {
        "Expected metadata.json, got $metadataPath"
    }

    val topLevel = mutableMapOf<String, String>()
    val rows = mutableListOf<BenchmarkMetadataJsonRow>()
    var inResults = false
    var currentRow: MutableMap<String, String>? = null

    Files.readAllLines(metadataPath).forEach { rawLine ->
        val line = rawLine.trim()
        when {
            line.startsWith("\"results\"") -> inResults = true
            inResults && line == "]" -> inResults = false
            inResults && line == "{" -> currentRow = mutableMapOf()
            inResults && line.startsWith("}") -> {
                currentRow?.toBenchmarkMetadataJsonRow()?.let(rows::add)
                currentRow = null
            }
            inResults -> {
                parseJsonProperty(line)?.let { property ->
                    currentRow?.put(property.first, property.second)
                }
            }
            else -> {
                parseJsonProperty(line)?.let { property ->
                    topLevel[property.first] = property.second
                }
            }
        }
    }

    return BenchmarkMetadataJsonRun(
        sourcePath = metadataPath,
        startedAt = topLevel.getValue("startedAt"),
        cpuFamily = topLevel.getValue("cpuFamily"),
        os = topLevel.getValue("os"),
        jdk = topLevel.getValue("jdk"),
        kotlinVersion = topLevel.getValue("kotlinVersion"),
        gradleInvocation = topLevel.getValue("gradleInvocation"),
        powerState = topLevel.getValue("powerState"),
        allocationTracking = topLevel.getValue("allocationTracking"),
        rows = rows,
    )
}

private fun Map<String, String>.toBenchmarkMetadataJsonRow(): BenchmarkMetadataJsonRow? {
    val id = get("id") ?: return null
    val name = get("name") ?: id
    val medianMs = get("medianMs")?.toDoubleOrNull() ?: return null
    return BenchmarkMetadataJsonRow(
        id = id,
        group = get("group").orEmpty(),
        name = name,
        kind = get("kind").orEmpty(),
        size = get("size").orEmpty(),
        chars = get("chars")?.toIntOrNull() ?: 0,
        lines = get("lines")?.toIntOrNull() ?: 0,
        spans = get("spans")?.toIntOrNull() ?: 0,
        warmups = get("warmups")?.toIntOrNull() ?: 0,
        samples = get("samples")?.toIntOrNull() ?: 0,
        medianMs = medianMs,
        p90Ms = get("p90Ms")?.toDoubleOrNull() ?: medianMs,
        medianAllocBytes = get("medianAllocBytes")?.takeUnless { it == "null" }?.toLongOrNull(),
        checksum = get("checksum")?.toLongOrNull() ?: 0L,
    )
}

internal fun parseJsonProperty(line: String): Pair<String, String>? {
    val key = line.substringAfter('"', missingDelimiterValue = "")
        .substringBefore('"', missingDelimiterValue = "")
        .takeIf { it.isNotEmpty() }
        ?: return null
    val rawValue = line.substringAfter(':', missingDelimiterValue = "")
        .trim()
        .removeSuffix(",")
        .trim()
    val value = if (rawValue.startsWith('"')) {
        decodeJsonString(rawValue)
    } else {
        rawValue
    }
    return key to value
}

internal fun decodeJsonString(rawValue: String): String {
    val body = rawValue.removeSurrounding("\"")
    return buildString {
        var index = 0
        while (index < body.length) {
            val char = body[index++]
            if (char != '\\' || index >= body.length) {
                append(char)
                continue
            }
            when (val escaped = body[index++]) {
                '\\' -> append('\\')
                '"' -> append('"')
                'n' -> append('\n')
                'r' -> append('\r')
                't' -> append('\t')
                else -> append(escaped)
            }
        }
    }
}
