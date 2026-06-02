package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.engine.language.LanguageExtension
import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizer

internal object EmbeddedSubstringBenchmarks {
    private const val TotalEmbeddedChars = 500 * 1024

    fun cases(): List<BenchmarkCase> =
        listOf(1, 10, 100, 1000).flatMap { regionCount ->
            val workload = embeddedWorkload(regionCount = regionCount)
            listOf(
                noCopyCase(workload),
                substringCopyCase(workload),
                publicRouteCase(workload),
            )
        }

    private fun noCopyCase(workload: EmbeddedSubstringWorkload): BenchmarkCase =
        BenchmarkCase(
            id = "diagnostics/embedded-substring/${workload.regionCount}-regions/range-walk-no-copy",
            group = "Embedded substring copy",
            name = "${workload.regionCount} regions range walk no copy",
            workloadKind = BenchmarkWorkloadKind.Micro,
            sizeName = workload.sizeName,
            inputChars = workload.code.length,
            inputLines = lineCount(workload.code),
        ) {
            BenchmarkIteration(
                spanCount = workload.regions.size,
                checksum = checksumRanges(workload.code, workload.regions),
            )
        }

    private fun substringCopyCase(workload: EmbeddedSubstringWorkload): BenchmarkCase =
        BenchmarkCase(
            id = "diagnostics/embedded-substring/${workload.regionCount}-regions/substring-copy-only",
            group = "Embedded substring copy",
            name = "${workload.regionCount} regions substring copy only",
            workloadKind = BenchmarkWorkloadKind.Micro,
            sizeName = workload.sizeName,
            inputChars = workload.code.length,
            inputLines = lineCount(workload.code),
        ) {
            var checksum = 0x577572B6F3D2501BL.mix(workload.regions.size)
            var copiedChars = 0
            workload.regions.forEach { region ->
                val childCode = workload.code.substring(region.start, region.endExclusive)
                copiedChars += childCode.length
                checksum = checksum
                    .mix(childCode.length)
                    .mix(childCode.firstOrNull()?.code ?: 0)
                    .mix(childCode.lastOrNull()?.code ?: 0)
            }
            BenchmarkIteration(
                spanCount = workload.regions.size,
                checksum = checksum.mix(copiedChars),
            )
        }

    private fun publicRouteCase(workload: EmbeddedSubstringWorkload): BenchmarkCase {
        val parentId = LanguageId.fromString("bench-substring-parent-${workload.regionCount}")
        val childId = LanguageId.fromString("bench-substring-child-${workload.regionCount}")
        val engine = SyntaxTokenizer(
            extensions = listOf(
                LanguageExtension(
                    languageId = parentId,
                    tokenizer = embeddedRoutingTokenizer(workload.regions, childId.value),
                ),
                LanguageExtension(
                    languageId = childId,
                    tokenizer = LanguageTokenizer { request ->
                        oneLargeSpan(request.code, request.languageId)
                    },
                ),
            ),
        )

        return BenchmarkCase(
            id = "diagnostics/embedded-substring/${workload.regionCount}-regions/public-embedded-route",
            group = "Embedded substring copy",
            name = "${workload.regionCount} regions public embedded route",
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
            sizeName = workload.sizeName,
            inputChars = workload.code.length,
            inputLines = lineCount(workload.code),
        ) {
            val spans = engine.tokenize(code = workload.code, languageLabel = parentId.value)
            BenchmarkIteration(
                spanCount = spans.size,
                checksum = checksumTokenSpans(workload.code, spans),
            )
        }
    }

    private fun embeddedRoutingTokenizer(
        regions: List<EmbeddedRange>,
        childLabel: String,
    ): LanguageTokenizer =
        LanguageTokenizer { request ->
            val spans = mutableListOf<SyntaxTokenSpan>()
            regions.forEach { region ->
                request.tokenizeEmbedded(
                    code = request.code.substring(region.start, region.endExclusive),
                    languageLabel = childLabel,
                ).forEach { childSpan ->
                    spans.add(
                        SyntaxTokenSpan(
                            start = region.start + childSpan.start,
                            endExclusive = region.start + childSpan.endExclusive,
                            role = childSpan.role,
                            languageId = childSpan.languageId,
                        ),
                    )
                }
            }
            spans
        }

    private fun embeddedWorkload(regionCount: Int): EmbeddedSubstringWorkload {
        val builder = StringBuilder(TotalEmbeddedChars + regionCount * 32)
        val regions = mutableListOf<EmbeddedRange>()
        val baseRegionChars = TotalEmbeddedChars / regionCount
        val remainderChars = TotalEmbeddedChars % regionCount

        repeat(regionCount) { index ->
            builder.append("<script type=\"module\">\n")
            val start = builder.length
            val regionChars = baseRegionChars + if (index < remainderChars) 1 else 0
            appendEmbeddedPayload(builder, targetChars = regionChars, index = index)
            val endExclusive = builder.length
            regions += EmbeddedRange(start = start, endExclusive = endExclusive)
            builder.append("\n</script>\n")
        }

        return EmbeddedSubstringWorkload(
            regionCount = regionCount,
            code = builder.toString(),
            regions = regions,
        )
    }

    private fun appendEmbeddedPayload(
        builder: StringBuilder,
        targetChars: Int,
        index: Int,
    ) {
        val start = builder.length
        val line = "const value$index = \"alpha beta gamma delta epsilon\";\n"
        while (builder.length - start < targetChars) {
            val remaining = targetChars - (builder.length - start)
            if (remaining >= line.length) {
                builder.append(line)
            } else {
                builder.append(line.take(remaining))
            }
        }
    }

    private fun checksumRanges(code: String, regions: List<EmbeddedRange>): Long {
        var checksum = 0x1137D55BD181F1F1L.mix(code.length).mix(regions.size)
        regions.forEach { region ->
            checksum = checksum
                .mix(region.start)
                .mix(region.endExclusive)
                .mix(region.endExclusive - region.start)
            if (region.endExclusive > region.start) {
                checksum = checksum
                    .mix(code[region.start].code)
                    .mix(code[region.endExclusive - 1].code)
            }
        }
        return checksum
    }

    private data class EmbeddedSubstringWorkload(
        val regionCount: Int,
        val code: String,
        val regions: List<EmbeddedRange>,
    ) {
        val sizeName: String =
            "${TotalEmbeddedChars / 1024} KiB embedded / $regionCount regions"
    }

    private data class EmbeddedRange(
        val start: Int,
        val endExclusive: Int,
    )
}
