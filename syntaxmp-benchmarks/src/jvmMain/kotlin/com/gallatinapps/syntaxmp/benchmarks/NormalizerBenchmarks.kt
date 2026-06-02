package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.engine.language.LanguageExtension
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer

internal object NormalizerBenchmarks {
    fun cases(): List<BenchmarkCase> {
        val code = syntheticCode(BenchmarkTargetSize.MediumEditorFile)
        val workloads = listOf(
            NormalizerWorkload("sorted non-overlapping", ::sortedNonOverlappingSpans),
            NormalizerWorkload("adjacent same role merge", ::adjacentSameRoleSpans),
            NormalizerWorkload("adjacent different role", ::adjacentDifferentRoleSpans),
            NormalizerWorkload("clipped out-of-range", ::clippedOutOfRangeSpans),
            NormalizerWorkload("out-of-order", ::outOfOrderSpans),
            NormalizerWorkload("broad parent narrow child", ::broadParentNarrowChildSpans),
            NormalizerWorkload("same-boundary overlaps", ::sameBoundaryOverlapSpans),
            NormalizerWorkload("unique boundaries active spans", ::uniqueBoundaryActiveSpans),
        )
        val extensions = workloads.map { workload ->
            LanguageExtension(
                languageId = workload.languageId,
                tokenizer = LanguageTokenizer { request ->
                    workload.spans(request.code.length, request.languageId)
                },
            )
        }
        val engine = SyntaxTokenizer(extensions = extensions)

        return workloads.map { workload ->
            BenchmarkCase(
                id = "diagnostics/normalizer/${stableIdSegment(workload.name)}",
                group = "Synthetic normalizer",
                name = workload.name,
                workloadKind = BenchmarkWorkloadKind.Synthetic,
                sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
                inputChars = code.length,
                inputLines = lineCount(code),
            ) {
                val spans = engine.tokenize(code = code, languageLabel = workload.languageId.value)
                BenchmarkIteration(
                    spanCount = spans.size,
                    checksum = checksumTokenSpans(code, spans),
                )
            }
        }
    }

    private data class NormalizerWorkload(
        val name: String,
        val spans: (Int, LanguageId) -> List<SyntaxTokenSpan>,
    ) {
        val languageId: LanguageId =
            LanguageId.fromString("bench-normalizer-${name.replace(Regex("[^a-zA-Z0-9]+"), "-")}")
    }

    private fun sortedNonOverlappingSpans(
        length: Int,
        languageId: LanguageId,
    ): List<SyntaxTokenSpan> =
        buildList {
            var start = 0
            while (start < length) {
                val end = (start + 6).coerceAtMost(length)
                add(span(start, end, SyntaxRole.Keyword, languageId))
                start += 12
            }
        }

    private fun adjacentSameRoleSpans(
        length: Int,
        languageId: LanguageId,
    ): List<SyntaxTokenSpan> =
        buildList {
            var start = 0
            while (start < length) {
                val end = (start + 8).coerceAtMost(length)
                add(span(start, end, SyntaxRole.String, languageId))
                start = end
            }
        }

    private fun adjacentDifferentRoleSpans(
        length: Int,
        languageId: LanguageId,
    ): List<SyntaxTokenSpan> =
        buildList {
            var start = 0
            var index = 0
            while (start < length) {
                val end = (start + 8).coerceAtMost(length)
                val role = if (index % 2 == 0) SyntaxRole.String else SyntaxRole.Number
                add(span(start, end, role, languageId))
                start = end
                index++
            }
        }

    private fun clippedOutOfRangeSpans(
        length: Int,
        languageId: LanguageId,
    ): List<SyntaxTokenSpan> =
        buildList {
            add(span(-64, 32, SyntaxRole.Comment, languageId))
            var start = 0
            while (start < length) {
                add(span(start - 4, start + 16, SyntaxRole.Keyword, languageId))
                start += 24
            }
            add(span(length - 16, length + 128, SyntaxRole.String, languageId))
            add(span(length + 2, length + 128, SyntaxRole.Number, languageId))
        }

    private fun outOfOrderSpans(
        length: Int,
        languageId: LanguageId,
    ): List<SyntaxTokenSpan> =
        sortedNonOverlappingSpans(length, languageId).asReversed()

    private fun broadParentNarrowChildSpans(
        length: Int,
        languageId: LanguageId,
    ): List<SyntaxTokenSpan> =
        buildList {
            add(span(0, length, SyntaxRole.Markup, languageId))
            var start = 0
            while (start < length) {
                add(span(start, (start + 10).coerceAtMost(length), SyntaxRole.Keyword.Declaration, languageId))
                start += 32
            }
        }

    private fun sameBoundaryOverlapSpans(
        length: Int,
        languageId: LanguageId,
    ): List<SyntaxTokenSpan> =
        buildList {
            var start = 0
            while (start < length) {
                val end = (start + 24).coerceAtMost(length)
                add(span(start, end, SyntaxRole.Markup, languageId))
                add(span(start, end, SyntaxRole.Keyword, languageId))
                add(span(start, end, SyntaxRole.Keyword.Declaration, languageId))
                add(span(start, end, SyntaxRole.String, languageId))
                start += 24
            }
        }

    private fun uniqueBoundaryActiveSpans(
        length: Int,
        languageId: LanguageId,
    ): List<SyntaxTokenSpan> =
        buildList {
            var start = 0
            while (start < length) {
                val wideEnd = (start + 128).coerceAtMost(length)
                val midEnd = (start + 96).coerceAtMost(length)
                val narrowEnd = (start + 64).coerceAtMost(length)
                add(span(start, wideEnd, SyntaxRole.Markup, languageId))
                add(span(start + 7, midEnd, SyntaxRole.Keyword, languageId))
                add(span(start + 13, narrowEnd, SyntaxRole.Keyword.Declaration, languageId))
                add(span(start + 21, (start + 48).coerceAtMost(length), SyntaxRole.String, languageId))
                start += 37
            }
        }

    private fun span(
        start: Int,
        endExclusive: Int,
        role: SyntaxRole,
        languageId: LanguageId,
    ): SyntaxTokenSpan =
        SyntaxTokenSpan(
            start = start,
            endExclusive = endExclusive,
            role = role,
            languageId = languageId,
        )
}
