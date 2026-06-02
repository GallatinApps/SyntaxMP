package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.benchmarks.fixtures.ExpandedLanguageBenchmarkFixture
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkCatalog
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer

internal object RepresentativeLanguageBenchmarks {
    private val defaultEngine = SyntaxTokenizer()

    fun cases(): List<BenchmarkCase> =
        LanguageBenchmarkCatalog.expandedFixtures().map(::tokenizationCase)

    private fun tokenizationCase(fixture: ExpandedLanguageBenchmarkFixture): BenchmarkCase {
        // Target-line ranges are validated by LanguageBenchmarkFixtureSelfCheck (a hard check at
        // startup), so by the time cases build, no fixture can be out of range. No per-case warning.
        return BenchmarkCase(
            id = fixture.caseId,
            group = "Representative language fixtures - ${fixture.family.displayName}",
            name = fixture.displayName,
            workloadKind = BenchmarkWorkloadKind.Representative,
            sizeName = fixture.sizeName,
            inputChars = fixture.chars,
            inputLines = fixture.lines,
        ) {
            val spans = defaultEngine.tokenize(code = fixture.code, languageLabel = fixture.languageLabel)
            BenchmarkIteration(
                spanCount = spans.size,
                checksum = checksumTokenSpans(fixture.code, spans),
            )
        }
    }
}
