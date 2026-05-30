package com.gallatinapps.syntaxmp.languages.svelte

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupDirectiveAttributeOptions
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionMarker
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupExpressionRule
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScanner
import com.gallatinapps.syntaxmp.engine.scanners.markup.MarkupScannerOptions

internal object SvelteTokenizer {
    private val scannerOptions = MarkupScannerOptions(
        rawTextTags = SvelteRawTextTags,
        rawTextLanguageForTag = MarkupScannerOptions::defaultRawTextLanguageForTag,
        expressionLanguage = SyntaxLanguageId.JavaScript,
        expressionRules = listOf(
            MarkupExpressionRule(
                opener = "{",
                closer = "}",
                allowedAtTopLevel = true,
                allowedInsideMarkup = true,
                marker = MarkupExpressionMarker.FirstCharKeyword(
                    chars = SvelteBlockMarkers,
                    role = SyntaxRole.Keyword,
                ),
            ),
        ),
        directiveAttributes = MarkupDirectiveAttributeOptions.ComponentDirectives,
        preserveTagNameCase = true,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult {
        val spans = MarkupScanner(
            request = request,
            options = scannerOptions,
        ).scan()
        return SyntaxTokenizeResult(
            addReactiveLabelSpans(code = request.code, language = request.languageId, spans = spans),
        )
    }
}

private val SvelteBlockMarkers = setOf('#', '/', ':')
private val SvelteReactiveRole = SyntaxRole.Keyword

private fun addReactiveLabelSpans(
    code: String,
    language: SyntaxLanguageId,
    spans: List<SyntaxTokenSpan>,
): List<SyntaxTokenSpan> {
    val reactiveSpans = findScriptBodyRanges(code)
        .flatMap { range -> findReactiveLabelSpans(code, language, range.start, range.endExclusive) }
    if (reactiveSpans.isEmpty()) return spans
    return (spans.filterNot { span -> reactiveSpans.any { reactive -> span.overlaps(reactive) } } + reactiveSpans)
        .sortedWith(compareBy<SyntaxTokenSpan> { it.start }.thenBy { it.endExclusive })
}

private fun findScriptBodyRanges(code: String): List<TextRange> =
    buildList {
        var cursor = 0
        while (cursor < code.length) {
            val open = code.indexOf("<script", startIndex = cursor, ignoreCase = true)
            if (open == -1) break
            val afterName = open + "<script".length
            val afterNameChar = code.getOrNull(afterName)
            if (afterNameChar != null && !afterNameChar.isWhitespace() && afterNameChar != '>') {
                cursor = afterName
                continue
            }
            val openEnd = findTagEnd(code = code, start = afterName) ?: break
            val close = code.indexOf("</script", startIndex = openEnd + 1, ignoreCase = true)
                .let { if (it == -1) code.length else it }
            add(TextRange(start = openEnd + 1, endExclusive = close))
            cursor = if (close == code.length) code.length else close + "</script".length
        }
    }

private fun findReactiveLabelSpans(
    code: String,
    language: SyntaxLanguageId,
    start: Int,
    endExclusive: Int,
): List<SyntaxTokenSpan> =
    buildList {
        var lineStart = start
        while (lineStart < endExclusive) {
            val lineEnd = code.indexOf('\n', startIndex = lineStart)
                .let { if (it == -1 || it > endExclusive) endExclusive else it }
            val markerStart = firstNonWhitespace(code = code, start = lineStart, endExclusive = lineEnd)
            if (
                markerStart + 1 < lineEnd &&
                code[markerStart] == '$' &&
                code[markerStart + 1] == ':'
            ) {
                add(
                    SyntaxTokenSpan(
                        start = markerStart,
                        endExclusive = markerStart + 2,
                        role = SvelteReactiveRole,
                        languageId = language,
                    ),
                )
            }
            lineStart = lineEnd + 1
        }
    }

private fun firstNonWhitespace(
    code: String,
    start: Int,
    endExclusive: Int,
): Int {
    var index = start
    while (index < endExclusive && code[index].isWhitespace()) index++
    return index
}

private fun findTagEnd(code: String, start: Int): Int? {
    var index = start
    var quote: Char? = null
    while (index < code.length) {
        val char = code[index]
        if (quote != null) {
            if (char == quote) quote = null
        } else if (char == '"' || char == '\'') {
            quote = char
        } else if (char == '>') {
            return index
        }
        index++
    }
    return null
}

private fun SyntaxTokenSpan.overlaps(other: SyntaxTokenSpan): Boolean =
    start < other.endExclusive && other.start < endExclusive

private data class TextRange(
    val start: Int,
    val endExclusive: Int,
)
