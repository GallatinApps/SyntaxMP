package com.gallatinapps.syntaxmp.languages.mdx

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupDirectiveAttributeOptions
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionRule
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions
import com.gallatinapps.syntaxmp.engine.spans.appendEmbeddedSpans
import com.gallatinapps.syntaxmp.languages.markdown.MarkdownScanner

internal object MdxTokenizer {
    private val scannerOptions = MarkupScannerOptions(
        rawTextTags = MdxRawTextTags,
        rawTextLanguageForTag = MarkupScannerOptions::defaultRawTextLanguageForTag,
        expressionLanguage = SyntaxLanguageId.JavaScript,
        expressionRules = listOf(
            MarkupExpressionRule(
                opener = "{",
                closer = "}",
                allowedAtTopLevel = true,
                allowedInsideMarkup = true,
            ),
        ),
        directiveAttributes = MarkupDirectiveAttributeOptions.ComponentDirectives,
        preserveTagNameCase = true,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult {
        val partition = partitionMdxRanges(request.code)
        return SyntaxTokenizeResult(
            buildList {
                appendMarkdownSpans(
                    request = request,
                    ranges = partition.markdownRanges,
                )
                appendMarkupSpans(
                    request = request,
                    ranges = partition.componentRanges + partition.expressionRanges,
                )
                appendMdxEsmSpans(
                    request = request,
                    ranges = partition.esmRanges,
                )
            },
        )
    }

    private fun MutableList<SyntaxTokenSpan>.appendMarkdownSpans(
        request: SyntaxTokenizeRequest,
        ranges: List<MdxSourceRange>,
    ) {
        val scanner = MarkdownScanner(
            request = request,
            htmlLanguage = SyntaxLanguageId.Html.value,
        )
        ranges.forEach { range ->
            addAll(scanner.scan(start = range.start, endExclusive = range.endExclusive))
        }
    }

    private fun MutableList<SyntaxTokenSpan>.appendMarkupSpans(
        request: SyntaxTokenizeRequest,
        ranges: List<MdxSourceRange>,
    ) {
        val scanner = MarkupScanner(
            request = request,
            options = scannerOptions,
        )
        ranges.forEach { range ->
            addAll(scanner.scan(start = range.start, endExclusive = range.endExclusive))
        }
    }

    private fun MutableList<SyntaxTokenSpan>.appendMdxEsmSpans(
        request: SyntaxTokenizeRequest,
        ranges: List<MdxSourceRange>,
    ) {
        ranges.forEach { range ->
            appendEmbeddedSpans(
                parentCode = request.code,
                bodyStart = range.start,
                bodyEnd = range.endExclusive,
                languageLabel = SyntaxLanguageId.JavaScript.value,
                tokenizeEmbedded = request::tokenizeEmbedded,
            )
        }
    }
}
