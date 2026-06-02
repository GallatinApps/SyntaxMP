package com.gallatinapps.syntaxmp.benchmarks

import com.gallatinapps.syntaxmp.language.LanguageExtension
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer

internal object TokenizationBenchmarks {
    private val defaultEngine = SyntaxTokenizer()

    fun diagnosticCases(): List<BenchmarkCase> =
        publicTokenizationCases() +
            ComponentRoutingBenchmarks.cases() +
            LargeFileTokenizationBenchmarks.cases() +
            KotlinTokenizationBenchmarks.cases() +
            embeddedRoutingCases() +
            extensionLookupCases()

    private fun publicTokenizationCases(): List<BenchmarkCase> {
        val samples = listOf(
            BenchmarkSamplesFactory.jsonLowSpanPayload(BenchmarkTargetSize.LargeFile),
            BenchmarkSamplesFactory.jsonLowSpanPayload(BenchmarkTargetSize.StressFile),
            BenchmarkSamplesFactory.typeScriptSpanDense(BenchmarkTargetSize.MediumEditorFile),
            BenchmarkSamplesFactory.typeScriptSpanDense(BenchmarkTargetSize.LargeFile),
            BenchmarkSamplesFactory.typeScriptLowSpanPayload(BenchmarkTargetSize.LargeFile),
            BenchmarkSamplesFactory.htmlWithRawText(BenchmarkTargetSize.LargeFile),
            BenchmarkSamplesFactory.dockerfileHeredoc(BenchmarkTargetSize.MediumEditorFile),
        )

        return samples.map { sample ->
            tokenizationCase(
                group = "Public engine tokenization",
                sample = sample,
                engine = defaultEngine,
            )
        }
    }

    private fun embeddedRoutingCases(): List<BenchmarkCase> {
        val noEmbeddedId = LanguageId.fromString("bench-embedded-none")
        val oneLargeParentId = LanguageId.fromString("bench-embedded-one-large-parent")
        val manySmallParentId = LanguageId.fromString("bench-embedded-many-small-parent")
        val depthParentId = LanguageId.fromString("bench-embedded-depth-parent")
        val childId = LanguageId.fromString("bench-embedded-child")
        val grandchildId = LanguageId.fromString("bench-embedded-grandchild")
        val greatGrandchildId = LanguageId.fromString("bench-embedded-great-grandchild")
        val overDepthId = LanguageId.fromString("bench-embedded-over-depth")
        val leafId = LanguageId.fromString("bench-embedded-leaf")
        val disabledParentId = LanguageId.fromString("bench-embedded-disabled-parent")
        val unknownParentId = LanguageId.fromString("bench-embedded-unknown-parent")

        val embeddedCode = repeatedToAtLeast(
            seed = "region alpha beta gamma delta epsilon zeta eta theta\n",
            targetChars = BenchmarkTargetSize.MediumEditorFile.targetChars,
        )
        val extensions = listOf(
            LanguageExtension(noEmbeddedId, tokenizer = localSpanTokenizer()),
            LanguageExtension(
                languageId = oneLargeParentId,
                tokenizer = embeddedTokenizer(childLabel = leafId.value, mode = EmbeddedMode.OneLarge),
            ),
            LanguageExtension(
                languageId = manySmallParentId,
                tokenizer = embeddedTokenizer(childLabel = leafId.value, mode = EmbeddedMode.ManySmall),
            ),
            LanguageExtension(
                languageId = depthParentId,
                tokenizer = embeddedTokenizer(childLabel = childId.value, mode = EmbeddedMode.OneLarge),
            ),
            LanguageExtension(
                languageId = childId,
                tokenizer = embeddedTokenizer(childLabel = grandchildId.value, mode = EmbeddedMode.ManySmall),
            ),
            LanguageExtension(
                languageId = grandchildId,
                tokenizer = embeddedTokenizer(childLabel = greatGrandchildId.value, mode = EmbeddedMode.OneLarge),
            ),
            LanguageExtension(
                languageId = greatGrandchildId,
                tokenizer = embeddedTokenizer(childLabel = overDepthId.value, mode = EmbeddedMode.OneLarge),
            ),
            LanguageExtension(overDepthId, tokenizer = localSpanTokenizer()),
            LanguageExtension(leafId, tokenizer = localSpanTokenizer()),
            LanguageExtension(
                languageId = disabledParentId,
                tokenizer = embeddedTokenizer(childLabel = "bench-disabled-child", mode = EmbeddedMode.OneLarge),
            ),
            LanguageExtension(
                languageId = unknownParentId,
                tokenizer = embeddedTokenizer(childLabel = "definitely-unknown-language", mode = EmbeddedMode.OneLarge),
            ),
        )
        val engine = SyntaxTokenizer(extensions = extensions)

        return listOf(
            embeddedCase("no embedded regions", noEmbeddedId.value, embeddedCode, engine),
            embeddedCase("one large embedded region", oneLargeParentId.value, embeddedCode, engine),
            embeddedCase("many small embedded regions", manySmallParentId.value, embeddedCode, engine),
            embeddedCase("nested regions to depth cap", depthParentId.value, embeddedCode, engine),
            embeddedCase("disabled child language", disabledParentId.value, embeddedCode, engine),
            embeddedCase("unknown raw-text language", unknownParentId.value, embeddedCode, engine),
        )
    }

    private fun extensionLookupCases(): List<BenchmarkCase> =
        listOf(0, 5, 50).flatMap { extensionCount ->
            val extensions = numberedExtensions(extensionCount)
            val engine = SyntaxTokenizer(extensions = extensions)
            val exactLabel = if (extensionCount == 0) "kotlin" else "bench-extension-${extensionCount - 1}"
            val aliasLabel = if (extensionCount == 0) "kt" else "bench-alias-${extensionCount - 1}"
            val missLabel = "bench-extension-missing"
            val lookupLabels = listOf(missLabel, exactLabel, aliasLabel)
            listOf(
                resolveLanguageCase(extensionCount, "miss/exact/alias", lookupLabels, engine),
                lookupTokenizationCase(extensionCount, "miss", missLabel, engine),
                lookupTokenizationCase(extensionCount, "exact id hit", exactLabel, engine),
                lookupTokenizationCase(extensionCount, "alias hit", aliasLabel, engine),
            )
        }

    private fun tokenizationCase(
        group: String,
        sample: SourceSample,
        engine: SyntaxTokenizer,
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
            val spans = engine.tokenize(code = sample.code, languageLabel = sample.languageLabel)
            BenchmarkIteration(
                spanCount = spans.size,
                checksum = checksumTokenSpans(sample.code, spans),
            )
        }

    private fun embeddedCase(
        name: String,
        languageLabel: String,
        code: String,
        engine: SyntaxTokenizer,
    ): BenchmarkCase =
        BenchmarkCase(
            id = "diagnostics/embedded-routing/${stableIdSegment(name)}",
            group = "Embedded routing/depth",
            name = name,
            workloadKind = BenchmarkWorkloadKind.EmbeddedRouting,
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            inputChars = code.length,
            inputLines = lineCount(code),
        ) {
            val spans = engine.tokenize(code = code, languageLabel = languageLabel)
            BenchmarkIteration(
                spanCount = spans.size,
                checksum = checksumTokenSpans(code, spans),
            )
        }

    private fun resolveLanguageCase(
        extensionCount: Int,
        name: String,
        labels: List<String>,
        engine: SyntaxTokenizer,
    ): BenchmarkCase {
        val lookupIterations = 10_000
        val inputChars = labels.sumOf { it.length } * lookupIterations
        return BenchmarkCase(
            id = "diagnostics/extension-lookup/${extensionCount}-extensions/resolve-${stableIdSegment(name)}",
            group = "Extension lookup",
            name = "$extensionCount extensions resolveLanguageId 10k $name",
            workloadKind = BenchmarkWorkloadKind.Micro,
            sizeName = "$extensionCount extensions",
            inputChars = inputChars,
            inputLines = 1,
        ) {
            var checksum = extensionCount.toLong()
            var hitCount = 0
            repeat(lookupIterations) { index ->
                val languageId = engine.resolveLanguageId(labels[index % labels.size])
                if (languageId != null) {
                    hitCount++
                    checksum = checksum.mix(languageId.value.hashCode())
                } else {
                    checksum = checksum.mix(index)
                }
            }
            BenchmarkIteration(spanCount = hitCount, checksum = checksum)
        }
    }

    private fun lookupTokenizationCase(
        extensionCount: Int,
        hitKind: String,
        label: String,
        engine: SyntaxTokenizer,
    ): BenchmarkCase {
        val code = "val value = 42\n"
        return BenchmarkCase(
            id = "diagnostics/extension-lookup/${extensionCount}-extensions/tokenize-${stableIdSegment(hitKind)}",
            group = "Extension lookup",
            name = "$extensionCount extensions tokenize $hitKind",
            workloadKind = BenchmarkWorkloadKind.Micro,
            sizeName = "$extensionCount extensions",
            inputChars = code.length,
            inputLines = lineCount(code),
        ) {
            val spans = engine.tokenize(code = code, languageLabel = label)
            BenchmarkIteration(
                spanCount = spans.size,
                checksum = checksumTokenSpans(code, spans),
            )
        }
    }

    private fun numberedExtensions(count: Int): List<LanguageExtension> =
        (0 until count).map { index ->
            val languageId = LanguageId.fromString("bench-extension-$index")
            LanguageExtension(
                languageId = languageId,
                aliases = setOf("bench-alias-$index"),
                tokenizer = localSpanTokenizer(),
            )
        }

    private fun localSpanTokenizer(): LanguageTokenizer =
        LanguageTokenizer { request ->
            shortLineSpans(request.code, request.languageId)
        }

    private fun embeddedTokenizer(
        childLabel: String,
        mode: EmbeddedMode,
    ): LanguageTokenizer =
        LanguageTokenizer { request ->
            val spans = mutableListOf<SyntaxTokenSpan>()
            embeddedRanges(request.code, mode).forEach { range ->
                spans.add(
                    SyntaxTokenSpan(
                        start = range.start,
                        endExclusive = range.endExclusive,
                        role = SyntaxRole.Markup,
                        languageId = request.languageId,
                    ),
                )
                request.tokenizeEmbedded(
                    code = request.code.substring(range.start, range.endExclusive),
                    languageLabel = childLabel,
                ).forEach { childSpan ->
                    spans.add(
                        SyntaxTokenSpan(
                            start = range.start + childSpan.start,
                            endExclusive = range.start + childSpan.endExclusive,
                            role = childSpan.role,
                            languageId = childSpan.languageId,
                        ),
                    )
                }
            }
            spans
        }

    private fun embeddedRanges(code: String, mode: EmbeddedMode): List<EmbeddedRange> =
        when (mode) {
            EmbeddedMode.OneLarge -> listOf(EmbeddedRange(start = 0, endExclusive = code.length))
            EmbeddedMode.ManySmall -> buildList {
                var start = 0
                while (start < code.length) {
                    val end = (start + 96).coerceAtMost(code.length)
                    add(EmbeddedRange(start = start, endExclusive = end))
                    start = end + 48
                }
            }
        }

    private data class EmbeddedRange(
        val start: Int,
        val endExclusive: Int,
    )

    private enum class EmbeddedMode {
        OneLarge,
        ManySmall,
    }
}
