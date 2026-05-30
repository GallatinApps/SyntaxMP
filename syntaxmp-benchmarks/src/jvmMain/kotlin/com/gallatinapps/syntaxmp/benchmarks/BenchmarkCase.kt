package com.gallatinapps.syntaxmp.benchmarks

internal data class BenchmarkCase(
    val id: String,
    val group: String,
    val name: String,
    val workloadKind: BenchmarkWorkloadKind,
    val sizeName: String,
    val inputChars: Int,
    val inputLines: Int,
    val warmupIterations: Int = defaultWarmupIterations(inputChars),
    val measuredIterations: Int = defaultMeasuredIterations(inputChars),
    val run: () -> BenchmarkIteration,
)

internal data class BenchmarkIteration(
    val spanCount: Int,
    val checksum: Long,
)

internal enum class BenchmarkTargetSize(
    val displayName: String,
    val targetChars: Int,
    val idSegment: String,
) {
    MediumEditorFile(displayName = "Medium editor file", targetChars = 50 * 1024, idSegment = "medium"),
    HighlightThreshold(displayName = "Highlight threshold file", targetChars = 250 * 1024, idSegment = "highlight-threshold"),
    LargeFile(displayName = "Large file", targetChars = 500 * 1024, idSegment = "large"),
    StressFile(displayName = "Stress file", targetChars = 2 * 1024 * 1024, idSegment = "stress"),
}

internal enum class BenchmarkWorkloadKind(
    val displayName: String,
) {
    Representative("Representative"),
    EmbeddedRouting("Embedded/raw-text routing"),
    LowSpanPayload("Low-span payload"),
    SpanDenseStress("Span-dense stress"),
    FocusedScannerShape("Focused scanner shape"),
    Synthetic("Synthetic"),
    Micro("Micro"),
}

internal fun stableIdSegment(value: String): String =
    buildString {
        var lastWasSeparator = false
        value.lowercase().forEach { char ->
            when {
                char in 'a'..'z' || char in '0'..'9' -> {
                    append(char)
                    lastWasSeparator = false
                }
                !lastWasSeparator -> {
                    append('-')
                    lastWasSeparator = true
                }
            }
        }
    }.trim('-')
        .ifEmpty { "case" }

private fun defaultWarmupIterations(inputChars: Int): Int =
    when {
        inputChars >= BenchmarkTargetSize.StressFile.targetChars -> 1
        inputChars >= BenchmarkTargetSize.LargeFile.targetChars -> 1
        else -> 2
    }

private fun defaultMeasuredIterations(inputChars: Int): Int =
    when {
        inputChars >= BenchmarkTargetSize.StressFile.targetChars -> 3
        inputChars >= BenchmarkTargetSize.LargeFile.targetChars -> 3
        else -> 5
    }
