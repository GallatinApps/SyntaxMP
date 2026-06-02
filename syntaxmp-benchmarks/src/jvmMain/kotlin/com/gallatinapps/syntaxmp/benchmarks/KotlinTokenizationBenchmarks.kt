package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer

internal object KotlinTokenizationBenchmarks {
    private val defaultEngine = SyntaxTokenizer()

    fun cases(): List<BenchmarkCase> {
        val samples = listOf(
            grouped(
                group = "Kotlin tokenization - compose scaling",
                sample = KotlinComposeSamples.notesDashboard(multiplier = 1),
            ),
            grouped(
                group = "Kotlin tokenization - compose scaling",
                sample = KotlinComposeSamples.notesDashboard(multiplier = 2),
            ),
            grouped(
                group = "Kotlin tokenization - compose scaling",
                sample = KotlinComposeSamples.notesDashboard(multiplier = 5),
            ),
            grouped(
                group = "Kotlin tokenization - span density",
                sample = BenchmarkSamplesFactory.kotlinSpanDense(BenchmarkTargetSize.MediumEditorFile),
            ),
            grouped(
                group = "Kotlin tokenization - string/interpolation",
                sample = KotlinFocusedSamples.tripleQuotedStringNoInterpolation(BenchmarkTargetSize.MediumEditorFile),
            ),
            grouped(
                group = "Kotlin tokenization - string/interpolation",
                sample = KotlinFocusedSamples.tripleQuotedStringSparseInterpolation(BenchmarkTargetSize.MediumEditorFile),
            ),
            grouped(
                group = "Kotlin tokenization - identifier/context",
                sample = KotlinFocusedSamples.manyImportPackageLines(BenchmarkTargetSize.MediumEditorFile),
            ),
            grouped(
                group = "Kotlin tokenization - identifier/context",
                sample = KotlinFocusedSamples.manyOrdinaryIdentifiers(BenchmarkTargetSize.MediumEditorFile),
            ),
            grouped(
                group = "Kotlin tokenization - identifier/context",
                sample = KotlinFocusedSamples.manyNamedArgumentsAndCallChains(BenchmarkTargetSize.MediumEditorFile),
            ),
            grouped(
                group = "Kotlin tokenization - identifier/context",
                sample = KotlinFocusedSamples.manyAnnotations(BenchmarkTargetSize.MediumEditorFile),
            ),
            grouped(
                group = "Kotlin tokenization - compose scaling",
                sample = KotlinComposeSamples.notesDashboard(multiplier = 10),
            ),
            grouped(
                group = "Kotlin tokenization - span density",
                sample = BenchmarkSamplesFactory.kotlinSpanDense(BenchmarkTargetSize.LargeFile),
            ),
            grouped(
                group = "Kotlin tokenization - string/interpolation",
                sample = KotlinFocusedSamples.tripleQuotedStringNoInterpolation(BenchmarkTargetSize.LargeFile),
            ),
            grouped(
                group = "Kotlin tokenization - string/interpolation",
                sample = KotlinFocusedSamples.tripleQuotedStringSparseInterpolation(BenchmarkTargetSize.LargeFile),
            ),
        )

        return samples.map { groupedSample ->
            tokenizationCase(
                group = groupedSample.group,
                sample = groupedSample.sample,
            )
        }
    }

    private fun grouped(group: String, sample: SourceSample): GroupedSourceSample =
        GroupedSourceSample(group = group, sample = sample)

    private fun tokenizationCase(
        group: String,
        sample: SourceSample,
    ): BenchmarkCase =
        BenchmarkCase(
            id = sample.caseId,
            group = group,
            name = sample.name,
            workloadKind = sample.workloadKind,
            sizeName = sample.sizeName,
            inputChars = sample.chars,
            inputLines = sample.lines,
        ) {
            val spans = defaultEngine.tokenize(code = sample.code, languageLabel = sample.languageLabel)
            BenchmarkIteration(
                spanCount = spans.size,
                checksum = checksumTokenSpans(sample.code, spans),
            )
        }
}

private data class GroupedSourceSample(
    val group: String,
    val sample: SourceSample,
)
