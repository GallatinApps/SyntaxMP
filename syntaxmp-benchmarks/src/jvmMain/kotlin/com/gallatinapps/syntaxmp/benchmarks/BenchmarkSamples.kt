package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.compose.SyntaxStyledSpan
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal data class BenchmarkSamples(
    val nanos: List<Long>,
) {
    val minMs: Double = nanos.minOrNull().orZero().toMillis()
    val medianMs: Double = nanos.percentile(0.5).toMillis()
    val p90Ms: Double = nanos.percentile(0.9).toMillis()
    val maxMs: Double = nanos.maxOrNull().orZero().toMillis()
}

internal data class BenchmarkByteSamples(
    val bytes: List<Long>,
) {
    val minBytes: Long = bytes.minOrNull().orZero()
    val medianBytes: Long = bytes.percentile(0.5)
    val p90Bytes: Long = bytes.percentile(0.9)
    val maxBytes: Long = bytes.maxOrNull().orZero()
}

internal data class SourceSample(
    val caseId: String,
    val name: String,
    val languageLabel: String,
    val code: String,
    val sizeName: String,
    val workloadKind: BenchmarkWorkloadKind,
) {
    val chars: Int = code.length
    val lines: Int = lineCount(code)
}

/**
 * Synthetic, procedurally generated stress inputs (structured JSON, large HTML, dockerfiles, etc.)
 * sized by [BenchmarkTargetSize]. These exercise mechanism-specific worst cases, not realistic
 * per-language code; for realistic baselines see the representative `fixtures/` matrix.
 */
internal object BenchmarkSamplesFactory {
    fun jsonStructured(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/json-structured/${size.idSegment}",
            name = "JSON structured ${size.displayName.lowercase()}",
            languageLabel = "json",
            code = jsonDocument(size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.Representative,
        )

    fun jsonLowSpanPayload(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/json-low-span/${size.idSegment}",
            name = "JSON low-span string payload ${size.displayName.lowercase()}",
            languageLabel = "json",
            code = jsonLargeStringDocument(size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.LowSpanPayload,
        )

    fun kotlinRepresentative(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/kotlin-representative/${size.idSegment}",
            name = "Kotlin representative source ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = repeatedToAtLeast(kotlinRepresentativeBlock, size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.Representative,
        )

    fun kotlinSpanDense(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/kotlin-span-dense/${size.idSegment}",
            name = "Kotlin span-dense repeated block ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = repeatedToAtLeast(kotlinBlock, size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.SpanDenseStress,
        )

    fun kotlinLowSpanPayload(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/kotlin-low-span-raw-string/${size.idSegment}",
            name = "Kotlin low-span raw-string payload ${size.displayName.lowercase()}",
            languageLabel = "kotlin",
            code = kotlinLargeRawStringDocument(size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.LowSpanPayload,
        )

    fun typeScriptSpanDense(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/typescript-span-dense/${size.idSegment}",
            name = "TypeScript span-dense repeated block ${size.displayName.lowercase()}",
            languageLabel = "typescript",
            code = repeatedToAtLeast(typeScriptBlock, size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.SpanDenseStress,
        )

    fun typeScriptLowSpanPayload(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/typescript-low-span-comment/${size.idSegment}",
            name = "TypeScript low-span block-comment payload ${size.displayName.lowercase()}",
            languageLabel = "typescript",
            code = typeScriptLargeCommentDocument(size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.LowSpanPayload,
        )

    fun htmlWithRawText(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/html-script-style/${size.idSegment}",
            name = "HTML script/style ${size.displayName.lowercase()}",
            languageLabel = "html",
            code = if (size.targetChars >= BenchmarkTargetSize.LargeFile.targetChars) {
                htmlLargeRawTextDocument(size.targetChars)
            } else {
                repeatedToAtLeast(htmlRawTextBlock, size.targetChars)
            },
            sizeName = size.displayName,
            workloadKind = if (size.targetChars >= BenchmarkTargetSize.LargeFile.targetChars) {
                BenchmarkWorkloadKind.LowSpanPayload
            } else {
                BenchmarkWorkloadKind.EmbeddedRouting
            },
        )

    fun markdownFences(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/markdown-fences/${size.idSegment}",
            name = "Markdown fences ${size.displayName.lowercase()}",
            languageLabel = "markdown",
            code = repeatedToAtLeast(markdownFenceBlock, size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )

    fun dockerfileHeredoc(size: BenchmarkTargetSize): SourceSample =
        SourceSample(
            caseId = "diagnostics/generated/dockerfile-heredoc/${size.idSegment}",
            name = "Dockerfile heredoc ${size.displayName.lowercase()}",
            languageLabel = "dockerfile",
            code = repeatedToAtLeast(dockerfileHeredocBlock, size.targetChars),
            sizeName = size.displayName,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
        )
}

internal fun repeatedToAtLeast(seed: String, targetChars: Int): String =
    buildString(targetChars + seed.length) {
        while (length < targetChars) {
            append(seed)
        }
    }

internal fun lineCount(code: String): Int =
    if (code.isEmpty()) 0 else code.count { it == '\n' } + 1

internal fun checksumTokenSpans(code: String, spans: List<SyntaxTokenSpan>): Long {
    var checksum = 0x4F1BBCDCBFA5401EL.mix(code.length).mix(lineCount(code))
    spans.forEach { span ->
        checksum = checksum
            .mix(span.start)
            .mix(span.endExclusive)
            .mix(span.role.value.hashCode())
            .mix(span.languageId.value.hashCode())
    }
    return checksum.mix(spans.size)
}

internal fun checksumStyledSpans(code: String, spans: List<SyntaxStyledSpan>): Long {
    var checksum = 0x1A976FDF7C4D7A13L.mix(code.length).mix(lineCount(code))
    spans.forEach { span ->
        checksum = checksum
            .mix(span.start)
            .mix(span.endExclusive)
            .mix(span.style.hashCode())
    }
    return checksum.mix(spans.size)
}

internal fun syntheticCode(size: BenchmarkTargetSize): String =
    repeatedToAtLeast("alpha beta gamma delta epsilon zeta eta theta\n", size.targetChars)

internal fun shortLineSpans(code: String, languageId: LanguageId): List<SyntaxTokenSpan> =
    buildList {
        var lineStart = 0
        code.splitToSequence('\n').forEach { line ->
            val tokenEnd = (lineStart + line.length).coerceAtMost(code.length)
            var start = lineStart
            while (start < tokenEnd) {
                val end = (start + 5).coerceAtMost(tokenEnd)
                add(
                    SyntaxTokenSpan(
                        start = start,
                        endExclusive = end,
                        role = if (start % 2 == 0) SyntaxRole.Keyword else SyntaxRole.String,
                        languageId = languageId,
                    ),
                )
                start = end + 1
            }
            lineStart += line.length + 1
        }
    }

internal fun oneLargeSpan(code: String, languageId: LanguageId): List<SyntaxTokenSpan> =
    listOf(
        SyntaxTokenSpan(
            start = 0,
            endExclusive = code.length,
            role = SyntaxRole.String,
            languageId = languageId,
        ),
    )

private fun jsonDocument(targetChars: Int): String =
    buildString(targetChars + 256) {
        append("[\n")
        var index = 0
        while (length < targetChars) {
            append("  {")
            append("\"id\":")
            append(index)
            append(",\"name\":\"item-")
            append(index)
            append("\",\"enabled\":")
            append(index % 2 == 0)
            append(",\"values\":[1,2,3,4],\"meta\":{\"kind\":\"bench\"}}")
            append(if (length + 4 < targetChars) ",\n" else "\n")
            index++
        }
        append("]\n")
    }

private fun jsonLargeStringDocument(targetChars: Int): String {
    val prefix = "{\n  \"kind\": \"large-string\",\n  \"payload\": \""
    val suffix = "\"\n}\n"
    return buildString(targetChars + suffix.length) {
        append(prefix)
        while (length + suffix.length < targetChars) {
            append("alpha beta gamma delta epsilon zeta eta theta ")
        }
        append(suffix)
    }
}

private fun kotlinLargeRawStringDocument(targetChars: Int): String {
    val prefix = """
        package bench.generated

        val payload = ""${'"'}
    """.trimIndent()
    val suffix = """
        ""${'"'}

        fun payloadLength(): Int = payload.length

    """.trimIndent()
    return buildString(targetChars + suffix.length) {
        append(prefix)
        append('\n')
        while (length + suffix.length < targetChars) {
            append("alpha beta gamma delta epsilon zeta eta theta\n")
        }
        append(suffix)
    }
}

private fun typeScriptLargeCommentDocument(targetChars: Int): String {
    val prefix = "/*\n"
    val suffix = """
        */

        export function payloadLength(): number {
          return 42
        }

    """.trimIndent()
    return buildString(targetChars + suffix.length) {
        append(prefix)
        while (length + suffix.length < targetChars) {
            append("alpha beta gamma delta epsilon zeta eta theta\n")
        }
        append(suffix)
    }
}

private fun htmlLargeRawTextDocument(targetChars: Int): String {
    val prefix = """
        <section class="bench-section">
          <script type="module">
            /*
    """.trimIndent()
    val suffix = """
            */
            export const payloadLength = 42
          </script>
          <style>
            .bench-section { color: #336699; }
          </style>
        </section>

    """.trimIndent()
    return buildString(targetChars + suffix.length) {
        append(prefix)
        append('\n')
        while (length + suffix.length < targetChars) {
            append("alpha beta gamma delta epsilon zeta eta theta\n")
        }
        append(suffix)
    }
}

private val kotlinRepresentativeBlock = """
    package bench.generated

    import kotlin.math.max

    data class MediaItem(
        val id: String,
        val title: String,
        val folder: String,
        val tags: Set<String>,
        val updatedAtEpochMillis: Long,
    )

    class MediaIndex(
        private val seed: List<MediaItem>,
    ) {
        private val cache: Map<String, MediaItem> = seed.associateBy { it.id }

        fun visibleItems(filter: String, limit: Int): List<MediaItem> =
            seed.asSequence()
                .filter { item ->
                    filter.isBlank() ||
                        item.title.contains(filter, ignoreCase = true) ||
                        item.tags.any { tag -> tag.contains(filter, ignoreCase = true) }
                }
                .sortedByDescending { item -> item.updatedAtEpochMillis }
                .take(max(limit, 0))
                .toList()

        fun summaryFor(id: String): String =
            cache[id]?.let { item ->
                "${'$'}{item.title} in ${'$'}{item.folder} (${ '$' }{item.tags.joinToString()})"
            } ?: "missing:${'$'}id"
    }

    private val noteTemplate = ""${'"'}
        # Media Note

        This payload gives the representative Kotlin sample a mix of declarations,
        lambdas, strings, and plain text without turning every line into tiny tokens.
    ""${'"'}.trimIndent()

    fun renderMediaNote(item: MediaItem): String =
        buildString {
            appendLine(noteTemplate)
            appendLine("title=${'$'}{item.title}")
            appendLine("folder=${'$'}{item.folder}")
            appendLine("tags=${'$'}{item.tags.joinToString("|")}")
        }

""".trimIndent() + "\n\n"

private val kotlinBlock = """
    package bench.generated

    data class BenchItem(
        val id: Int,
        val title: String,
        val enabled: Boolean,
    )

    fun renderBenchItem(item: BenchItem): String {
        val prefix = if (item.enabled) "enabled" else "disabled"
        return "${'$'}prefix:${'$'}{item.id}:${'$'}{item.title.lowercase()}"
    }

""".trimIndent() + "\n\n"

private val typeScriptBlock = """
    export type BenchItem = {
      id: number
      title: string
      enabled: boolean
      values: Array<number>
    }

    export function renderBenchItem(item: BenchItem): string {
      const prefix = item.enabled ? "enabled" : "disabled"
      return `${'$'}{prefix}:${'$'}{item.id}:${'$'}{item.title.toLowerCase()}`
    }

""".trimIndent() + "\n\n"

private val htmlRawTextBlock = """
    <section class="bench-section">
      <style>
        .bench-card { display: grid; grid-template-columns: 1fr auto; color: #336699; }
        .bench-card[data-active="true"]::before { content: "active"; }
      </style>
      <script type="module">
        export function activate(node) {
          const label = node.dataset.label ?? "missing";
          return label.toUpperCase();
        }
      </script>
    </section>

""".trimIndent() + "\n\n"

private val markdownFenceBlock = """
    ## Benchmark Section

    Paragraph text with `inline code`, **strong text**, and [a link](https://syntaxmp.example).

    ```kotlin
    fun highlighted(value: String): String = value.trim().uppercase()
    ```

    ```json
    {"enabled": true, "count": 42, "tags": ["syntax", "bench"]}
    ```

""".trimIndent() + "\n\n"

private val dockerfileHeredocBlock = """
    FROM alpine:3.20
    RUN <<'SH'
    set -eu
    apk add --no-cache bash curl
    for file in /etc/profile /etc/shells; do
      echo "checking ${'$'}file"
    done
    SH

""".trimIndent() + "\n\n"

private fun Long?.orZero(): Long = this ?: 0L

private fun Long.toMillis(): Double = this / 1_000_000.0
