package com.gallatinapps.syntaxmp.benchmarks

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.toTextFieldBuffer
import com.gallatinapps.syntaxmp.compose.applySyntaxStyledSpans
import com.gallatinapps.syntaxmp.compose.buildSyntaxAnnotatedString
import com.gallatinapps.syntaxmp.compose.buildSyntaxStyledSpans
import com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme
import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.SyntaxTokenizer

internal object ComposeSpanBenchmarks {
    private val engine = SyntaxTokenizer()
    private val theme = SyntaxTheme.DefaultLight

    fun cases(): List<BenchmarkCase> {
        val syntheticCode = syntheticCode(BenchmarkTargetSize.MediumEditorFile)
        val syntheticLanguage = LanguageId.fromString("bench-compose")
        val manyShort = ComposeWorkload(
            sourceCaseId = "synthetic-many-short-one-line-spans",
            name = "many short one-line spans",
            code = syntheticCode,
            spans = shortLineSpans(syntheticCode, syntheticLanguage),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.Synthetic,
        )
        val oneLarge = ComposeWorkload(
            sourceCaseId = "synthetic-one-large-multiline-span",
            name = "one large multiline span",
            code = syntheticCode,
            spans = oneLargeSpan(syntheticCode, syntheticLanguage),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.Synthetic,
        )
        val markdown = tokenizedWorkload(BenchmarkSamplesFactory.markdownFences(BenchmarkTargetSize.MediumEditorFile))
        val html = tokenizedWorkload(BenchmarkSamplesFactory.htmlWithRawText(BenchmarkTargetSize.MediumEditorFile))
        val manyLines = ComposeWorkload(
            sourceCaseId = "synthetic-many-small-spans-across-lines",
            name = "many small spans across lines",
            code = repeatedToAtLeast(
                seed = "first second third fourth fifth sixth seventh eighth ninth tenth\r\n",
                targetChars = BenchmarkTargetSize.MediumEditorFile.targetChars,
            ),
            spans = shortLineSpans(syntheticCode, syntheticLanguage),
            sizeName = BenchmarkTargetSize.MediumEditorFile.displayName,
            workloadKind = BenchmarkWorkloadKind.Synthetic,
        ).let { workload ->
            workload.copy(spans = shortLineSpans(workload.code, syntheticLanguage))
        }

        val syntheticWorkloads = listOf(manyShort, oneLarge, manyLines)
        val tokenizedWorkloads = listOf(markdown, html)

        return syntheticWorkloads.flatMap(::spanBuildingOnlyCases) +
            tokenizedWorkloads.flatMap { workload ->
                spanBuildingOnlyCases(workload) + listOf(
                    tokenizePlusAnnotatedCase(workload),
                    tokenizePlusStyledSpansCase(workload),
                )
            }
    }

    private fun tokenizedWorkload(sample: SourceSample): ComposeWorkload {
        val spans = engine.tokenize(code = sample.code, languageLabel = sample.languageLabel)
        return ComposeWorkload(
            sourceCaseId = sample.caseId,
            name = sample.name,
            code = sample.code,
            languageLabel = sample.languageLabel,
            spans = spans,
            sizeName = sample.sizeName,
            workloadKind = sample.workloadKind,
        )
    }

    private fun spanBuildingOnlyCases(workload: ComposeWorkload): List<BenchmarkCase> =
        listOf(
            annotatedStringCase(workload),
            styledSpansCase(workload),
            textFieldBufferApplyCase(workload),
        )

    private fun annotatedStringCase(workload: ComposeWorkload): BenchmarkCase =
        BenchmarkCase(
            id = "diagnostics/compose-span-building/${workload.caseIdSegment}/annotated-string",
            group = "Compose span-building",
            name = "buildSyntaxAnnotatedString ${workload.name}",
            workloadKind = workload.workloadKind,
            sizeName = workload.sizeName,
            inputChars = workload.code.length,
            inputLines = lineCount(workload.code),
        ) {
            val annotatedString = buildSyntaxAnnotatedString(
                code = workload.code,
                spans = workload.spans,
                theme = theme,
            )
            BenchmarkIteration(
                spanCount = annotatedString.spanStyles.size,
                checksum = workload.code.length.toLong()
                    .mix(annotatedString.text.length)
                    .mix(annotatedString.spanStyles.size),
            )
        }

    private fun styledSpansCase(workload: ComposeWorkload): BenchmarkCase =
        BenchmarkCase(
            id = "diagnostics/compose-span-building/${workload.caseIdSegment}/styled-spans",
            group = "Compose span-building",
            name = "buildSyntaxStyledSpans ${workload.name}",
            workloadKind = workload.workloadKind,
            sizeName = workload.sizeName,
            inputChars = workload.code.length,
            inputLines = lineCount(workload.code),
        ) {
            val styledSpans = buildSyntaxStyledSpans(
                code = workload.code,
                spans = workload.spans,
                theme = theme,
            )
            BenchmarkIteration(
                spanCount = styledSpans.size,
                checksum = checksumStyledSpans(workload.code, styledSpans),
            )
        }

    private fun textFieldBufferApplyCase(workload: ComposeWorkload): BenchmarkCase =
        BenchmarkCase(
            id = "diagnostics/compose-span-building/${workload.caseIdSegment}/textfield-buffer-apply",
            group = "Compose span-building",
            name = "TextFieldBuffer.applySyntaxStyledSpans ${workload.name}",
            workloadKind = workload.workloadKind,
            sizeName = workload.sizeName,
            inputChars = workload.code.length,
            inputLines = lineCount(workload.code),
        ) {
            val styledSpans = buildSyntaxStyledSpans(
                code = workload.code,
                spans = workload.spans,
                theme = theme,
            )
            val buffer = TextFieldState(initialText = workload.code).toTextFieldBuffer()
            buffer.applySyntaxStyledSpans(styledSpans)
            BenchmarkIteration(
                spanCount = styledSpans.size,
                checksum = checksumStyledSpans(workload.code, styledSpans)
                    .mix(buffer.length),
            )
        }

    private fun tokenizePlusAnnotatedCase(workload: ComposeWorkload): BenchmarkCase =
        BenchmarkCase(
            id = "diagnostics/compose-span-building/${workload.caseIdSegment}/tokenize-plus-annotated-string",
            group = "Compose span-building",
            name = "engine.tokenize + buildSyntaxAnnotatedString ${workload.name}",
            workloadKind = workload.workloadKind,
            sizeName = workload.sizeName,
            inputChars = workload.code.length,
            inputLines = lineCount(workload.code),
        ) {
            val spans = engine.tokenize(code = workload.code, languageLabel = workload.languageLabel)
            val annotatedString = buildSyntaxAnnotatedString(
                code = workload.code,
                spans = spans,
                theme = theme,
            )
            BenchmarkIteration(
                spanCount = annotatedString.spanStyles.size,
                checksum = checksumTokenSpans(workload.code, spans)
                    .mix(annotatedString.spanStyles.size),
            )
        }

    private fun tokenizePlusStyledSpansCase(workload: ComposeWorkload): BenchmarkCase =
        BenchmarkCase(
            id = "diagnostics/compose-span-building/${workload.caseIdSegment}/tokenize-plus-styled-spans",
            group = "Compose span-building",
            name = "engine.tokenize + buildSyntaxStyledSpans ${workload.name}",
            workloadKind = workload.workloadKind,
            sizeName = workload.sizeName,
            inputChars = workload.code.length,
            inputLines = lineCount(workload.code),
        ) {
            val spans = engine.tokenize(code = workload.code, languageLabel = workload.languageLabel)
            val styledSpans = buildSyntaxStyledSpans(
                code = workload.code,
                spans = spans,
                theme = theme,
            )
            BenchmarkIteration(
                spanCount = styledSpans.size,
                checksum = checksumStyledSpans(workload.code, styledSpans),
            )
        }

    private data class ComposeWorkload(
        val sourceCaseId: String,
        val name: String,
        val code: String,
        val languageLabel: String = "",
        val spans: List<SyntaxTokenSpan>,
        val sizeName: String,
        val workloadKind: BenchmarkWorkloadKind,
    ) {
        val caseIdSegment: String =
            sourceCaseId.substringAfter("diagnostics/", sourceCaseId).replace('/', '-')
    }
}
