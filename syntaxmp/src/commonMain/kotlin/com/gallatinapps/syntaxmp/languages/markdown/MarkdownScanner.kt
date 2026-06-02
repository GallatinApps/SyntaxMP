package com.gallatinapps.syntaxmp.languages.markdown

import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.scanners.markup.findMarkupEntityEnd
import com.gallatinapps.syntaxmp.engine.spans.appendEmbeddedSpans
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest

internal class MarkdownScanner(
    private val request: TokenizeRequest,
    private val htmlLanguage: String? = null,
) {
    private val code: String = request.code
    private val language: LanguageId = request.languageId
    private val tokens = mutableListOf<SyntaxTokenSpan>()
    private val inlineSkipRanges = mutableListOf<MarkdownInlineSkipRange>()
    private val htmlRangeFinder = MarkdownHtmlRangeFinder(code)

    fun scan(
        start: Int = 0,
        endExclusive: Int = code.length,
    ): List<SyntaxTokenSpan> {
        tokens.clear()
        inlineSkipRanges.clear()
        val safeStart = start.coerceIn(0, code.length)
        val safeEnd = endExclusive.coerceIn(safeStart, code.length)
        var lineStart = safeStart
        var inFence = false
        var fenceChar: Char? = null
        var fenceLength = 0
        var fenceLanguage: String? = null
        var fenceBodyStart = -1
        var paragraphOpen = false
        var inTable = false
        while (lineStart <= safeEnd) {
            val lineEnd = lineEnd(lineStart, safeEnd)
            val fence = findFence(lineStart, lineEnd)
            if (!inFence) {
                if (fence != null) {
                    addFenceTokens(fence, lineEnd)
                    inFence = true
                    fenceChar = fence.marker
                    fenceLength = fence.length
                    fenceLanguage = fence.language
                    fenceBodyStart = if (lineEnd < safeEnd) lineEnd + 1 else safeEnd
                    paragraphOpen = false
                    inTable = false
                } else {
                    val lineResult = scanLine(
                        start = lineStart,
                        end = lineEnd,
                        rangeEnd = safeEnd,
                        paragraphOpen = paragraphOpen,
                        inTable = inTable,
                    )
                    val consumedEnd = lineResult.consumedEnd
                    if (consumedEnd != null) {
                        if (consumedEnd >= safeEnd) break
                        lineStart = consumedEnd
                        paragraphOpen = lineResult.paragraphOpen
                        inTable = lineResult.inTable
                        continue
                    }
                    paragraphOpen = lineResult.paragraphOpen
                    inTable = lineResult.inTable
                }
            } else if (
                fence != null &&
                fence.marker == fenceChar &&
                fence.length >= fenceLength &&
                fence.language == null
            ) {
                appendFenceBodySpans(
                    language = fenceLanguage,
                    bodyStart = fenceBodyStart,
                    bodyEnd = lineStart,
                )
                addFenceTokens(fence, lineEnd)
                inFence = false
                fenceChar = null
                fenceLength = 0
                fenceLanguage = null
                fenceBodyStart = -1
                paragraphOpen = false
                inTable = false
            }
            if (lineEnd == safeEnd) {
                if (inFence) {
                    appendFenceBodySpans(
                        language = fenceLanguage,
                        bodyStart = fenceBodyStart,
                        bodyEnd = safeEnd,
                    )
                    inFence = false
                }
                break
            }
            lineStart = lineEnd + 1
        }
        return tokens.toList()
    }

    private fun findFence(start: Int, end: Int): FenceLine? {
        val contentStart = firstNonWhitespace(start, end)
        if (contentStart + 2 >= end) return null
        val marker = code[contentStart]
        if (marker != '`' && marker != '~') return null
        var markerEnd = contentStart
        while (markerEnd < end && code[markerEnd] == marker) markerEnd++
        val markerLength = markerEnd - contentStart
        if (markerLength < 3) return null
        val languageStart = firstNonWhitespace(markerEnd, end)
        val info = if (languageStart < end) code.substring(languageStart, end) else ""
        return FenceLine(
            marker = marker,
            length = markerLength,
            markerStart = contentStart,
            markerEnd = markerEnd,
            languageStart = languageStart.takeIf { it < end },
            language = parseFenceLanguage(info),
        )
    }

    private fun addFenceTokens(fence: FenceLine, lineEnd: Int) {
        add(fence.markerStart, fence.markerEnd, SyntaxRole.Markup.append("fence"))
        fence.languageStart?.let { languageStart ->
            add(languageStart, lineEnd, SyntaxRole.String.Language)
        }
    }

    private fun appendFenceBodySpans(
        language: String?,
        bodyStart: Int,
        bodyEnd: Int,
    ) {
        if (language == null || bodyStart < 0) return
        tokens.appendEmbeddedSpans(
            parentCode = request.code,
            bodyStart = bodyStart,
            bodyEnd = bodyEnd,
            languageLabel = language,
            tokenizeEmbedded = request::tokenizeEmbedded,
        )
    }

    private fun scanLine(
        start: Int,
        end: Int,
        rangeEnd: Int,
        paragraphOpen: Boolean,
        inTable: Boolean,
    ): MarkdownLineScanResult {
        val contentStart = firstNonWhitespace(start, end)
        if (contentStart >= end) return MarkdownLineScanResult(paragraphOpen = false)
        if (contentStart - start >= 4) return MarkdownLineScanResult(paragraphOpen = false)
        if (paragraphOpen && scanSetextHeadingUnderline(contentStart, end)) {
            return MarkdownLineScanResult(paragraphOpen = false)
        }
        if (scanThematicBreak(contentStart, end)) {
            return MarkdownLineScanResult(paragraphOpen = false)
        }
        if (scanLinkReferenceDefinition(contentStart, end)) {
            return MarkdownLineScanResult(paragraphOpen = false)
        }
        val blockMarker = scanBlockMarker(contentStart, end)
        scanTaskListMarker(contentStart, end)
        scanBlockHtml(
            start = contentStart,
            lineStart = start,
            lineEnd = end,
            rangeEnd = rangeEnd,
            canInterruptParagraph = !paragraphOpen,
        )?.let { consumedEnd ->
            return MarkdownLineScanResult(
                consumedEnd = consumedEnd,
                paragraphOpen = false,
                inTable = false,
            )
        }
        val tableLine = scanTableLine(start, end, rangeEnd, inTable)
        scanInlineMarkers(start, end)
        return MarkdownLineScanResult(
            paragraphOpen = !blockMarker && !tableLine,
            inTable = tableLine,
        )
    }

    private fun scanBlockMarker(start: Int, end: Int): Boolean =
        when {
            start < end && code[start] == '#' -> {
                var markerEnd = start
                while (markerEnd < end && code[markerEnd] == '#') markerEnd++
                val level = markerEnd - start
                if (level in 1..6 && markerEnd < end && code[markerEnd].isWhitespace()) {
                    add(start, markerEnd, markdownHeadingRole(level))
                    true
                } else {
                    false
                }
            }
            start < end && code[start] == '>' -> {
                add(start, start + 1, SyntaxRole.Markup.append("quote"))
                true
            }
            isListMarker(start, end) -> {
                val markerEnd = listMarkerEnd(start, end)
                add(start, markerEnd, SyntaxRole.Markup.append("list"))
                true
            }
            else -> false
        }

    private fun scanSetextHeadingUnderline(start: Int, end: Int): Boolean {
        if (start >= end || code[start] !in "=-") return false
        val marker = code[start]
        var markerEnd = start
        while (markerEnd < end && code[markerEnd] == marker) markerEnd++
        if (markerEnd == start || code.substring(markerEnd, end).isNotBlank()) return false
        add(start, markerEnd, markdownHeadingRole(if (marker == '=') 1 else 2))
        return true
    }

    private fun scanThematicBreak(start: Int, end: Int): Boolean {
        if (start >= end || code[start] !in "-*_") return false
        val marker = code[start]
        var count = 0
        var index = start
        var markerEnd = start
        while (index < end) {
            when {
                code[index] == marker -> {
                    count++
                    markerEnd = index + 1
                }
                code[index].isWhitespace() -> Unit
                else -> return false
            }
            index++
        }
        if (count < 3) return false
        add(start, markerEnd, SyntaxRole.Markup.append("thematic-break"))
        return true
    }

    private fun scanLinkReferenceDefinition(start: Int, end: Int): Boolean {
        if (start >= end || code[start] != '[') return false
        val labelEnd = code.indexOf(']', start + 1).takeIf { it != -1 && it + 1 < end } ?: return false
        if (code[labelEnd + 1] != ':') return false
        add(start, labelEnd + 2, SyntaxRole.Markup.append("reference"))

        var valueStart = firstNonWhitespace(labelEnd + 2, end)
        if (valueStart >= end) return true
        val valueEnd = if (code[valueStart] == '<') {
            code.indexOf('>', valueStart + 1)
                .takeIf { it != -1 && it < end }
                ?.plus(1)
                ?: end
        } else {
            var index = valueStart
            while (index < end && !code[index].isWhitespace()) index++
            index
        }
        add(valueStart, valueEnd, SyntaxRole.String.Url)

        valueStart = firstNonWhitespace(valueEnd, end)
        if (valueStart < end) {
            add(valueStart, end, SyntaxRole.String)
        }
        return true
    }

    private fun scanTaskListMarker(start: Int, end: Int) {
        if (!isListMarker(start, end)) return
        var index = listMarkerEnd(start, end)
        while (index < end && code[index].isWhitespace()) index++
        if (
            index + 2 < end &&
            code[index] == '[' &&
            code[index + 1] in " xX" &&
            code[index + 2] == ']'
        ) {
            val taskRole = if (code[index + 1] == ' ') {
                MarkdownTaskUncheckedRole
            } else {
                MarkdownTaskCheckedRole
            }
            add(index, index + 3, taskRole)
            inlineSkipRanges += MarkdownInlineSkipRange(start = index, endExclusive = index + 3)
        }
    }

    private fun scanTableLine(
        start: Int,
        end: Int,
        rangeEnd: Int,
        inTable: Boolean,
    ): Boolean {
        val tableLine = when {
            inTable && lineContainsTablePipe(start, end) -> true
            !inTable && lineContainsTablePipe(start, end) && nextLineIsTableDelimiter(end, rangeEnd) -> true
            else -> false
        }
        if (!tableLine) return false
        scanTableMarkers(start, end)
        return true
    }

    private fun scanTableMarkers(start: Int, end: Int) {
        var index = start
        while (index < end) {
            if (code[index] == '|') {
                add(index, index + 1, SyntaxRole.Markup.append("table"))
            }
            index++
        }
        if (!isTableDelimiterRow(start, end)) return
        index = start
        while (index < end) {
            if (code[index] == ':' || code[index] == '-') {
                val runStart = index
                var hasDash = false
                while (index < end && (code[index] == ':' || code[index] == '-')) {
                    hasDash = hasDash || code[index] == '-'
                    index++
                }
                if (hasDash) {
                    add(runStart, index, SyntaxRole.Markup.append("table"))
                }
            } else {
                index++
            }
        }
    }

    private fun nextLineIsTableDelimiter(lineEnd: Int, rangeEnd: Int): Boolean {
        val nextStart = nextLineStart(lineEnd, rangeEnd) ?: return false
        val nextEnd = lineEnd(nextStart, rangeEnd)
        return isTableDelimiterRow(nextStart, nextEnd)
    }

    private fun isTableDelimiterRow(start: Int, end: Int): Boolean {
        var index = firstNonWhitespace(start, end)
        var sawDash = false
        var sawPipe = false
        while (index < end) {
            when {
                code[index] == '|' -> {
                    sawPipe = true
                    index++
                }
                code[index] == ':' -> index++
                code[index] == '-' -> {
                    sawDash = true
                    while (index < end && code[index] == '-') index++
                }
                code[index].isWhitespace() -> index++
                else -> return false
            }
        }
        return sawPipe && sawDash
    }

    private fun lineContainsTablePipe(start: Int, end: Int): Boolean {
        var index = start
        while (index < end) {
            if (code[index] == '|') return true
            index++
        }
        return false
    }

    private fun scanInlineMarkers(start: Int, end: Int) {
        var index = start
        while (index < end) {
            inlineSkipEnd(index)?.let { skipEnd ->
                index = skipEnd
                continue
            }
            val char = code[index]
            index = when {
                char == '`' -> scanInlineCode(index, end)
                char == '\\' -> scanBackslashEscape(index, end) ?: (index + 1)
                char == '&' -> scanMarkdownEntity(index, end) ?: (index + 1)
                char == '~' && index + 1 < end && code[index + 1] == '~' -> scanStrikethrough(index, end)
                char == '<' -> scanEmbeddedHtml(index, end) ?: (index + 1)
                char == '!' && index + 1 < end && code[index + 1] == '[' -> {
                    add(index, index + 2, SyntaxRole.Markup.append("image"))
                    index + 2
                }
                char == '[' || char == ']' || char == '(' || char == ')' -> {
                    add(index, index + 1, SyntaxRole.Markup.append("link"))
                    index + 1
                }
                char == '*' || char == '_' -> scanEmphasis(index, end)
                else -> index + 1
            }
        }
    }

    private fun inlineSkipEnd(index: Int): Int? =
        inlineSkipRanges.firstOrNull { range -> index >= range.start && index < range.endExclusive }
            ?.endExclusive

    private fun scanInlineCode(start: Int, end: Int): Int {
        var markerEnd = start
        while (markerEnd < end && code[markerEnd] == '`') markerEnd++
        val marker = code.substring(start, markerEnd)
        val closing = code.indexOf(marker, markerEnd).takeIf { it != -1 && it < end }
        add(start, markerEnd, SyntaxRole.Markup.append("code"))
        if (closing != null) {
            add(closing, closing + marker.length, SyntaxRole.Markup.append("code"))
            return closing + marker.length
        }
        return markerEnd
    }

    private fun scanBackslashEscape(start: Int, end: Int): Int? {
        if (start + 1 >= end || code[start + 1] !in MarkdownEscapablePunctuation) return null
        add(start, start + 2, SyntaxRole.Escape)
        return start + 2
    }

    private fun scanMarkdownEntity(start: Int, end: Int): Int? {
        val entityEnd = findMarkupEntityEnd(code = code, start = start) ?: return null
        if (entityEnd > end) return null
        add(start, entityEnd, SyntaxRole.Escape)
        return entityEnd
    }

    private fun scanStrikethrough(start: Int, end: Int): Int {
        val closing = code.indexOf("~~", start + 2).takeIf { it != -1 && it < end }
        add(start, start + 2, SyntaxRole.Markup.append("strikethrough"))
        if (closing != null) {
            add(closing, closing + 2, SyntaxRole.Markup.append("strikethrough"))
            return closing + 2
        }
        return start + 2
    }

    private fun scanEmbeddedHtml(start: Int, end: Int): Int? {
        val range = htmlRangeFinder.findInlineRange(start = start, endExclusive = end)
        return appendEmbeddedHtmlRange(range)
    }

    private fun scanBlockHtml(
        start: Int,
        lineStart: Int,
        lineEnd: Int,
        rangeEnd: Int,
        canInterruptParagraph: Boolean,
    ): Int? {
        val range = htmlRangeFinder.findBlockRange(
            start = start,
            lineStart = lineStart,
            lineEnd = lineEnd,
            endExclusive = rangeEnd,
            canInterruptParagraph = canInterruptParagraph,
        )
        return appendEmbeddedHtmlRange(range)
    }

    private fun appendEmbeddedHtmlRange(range: MarkdownHtmlRange?): Int? {
        val htmlRange = range ?: return null
        val language = htmlLanguage ?: return null
        tokens.appendEmbeddedSpans(
            parentCode = request.code,
            bodyStart = htmlRange.start,
            bodyEnd = htmlRange.endExclusive,
            languageLabel = language,
            tokenizeEmbedded = request::tokenizeEmbedded,
        )
        return htmlRange.resumeAt
    }

    private fun scanEmphasis(start: Int, end: Int): Int {
        val marker = code[start]
        var markerEnd = start
        while (markerEnd < end && code[markerEnd] == marker) markerEnd++
        val length = (markerEnd - start).coerceAtMost(3)
        add(start, start + length, SyntaxRole.Markup.append("emphasis"))
        return markerEnd
    }

    private fun isListMarker(start: Int, end: Int): Boolean {
        if (start >= end) return false
        if (code[start] in "-*+" && start + 1 < end && code[start + 1].isWhitespace()) return true
        var cursor = start
        while (cursor < end && code[cursor].isDigit()) cursor++
        return cursor > start &&
            cursor < end &&
            code[cursor] == '.' &&
            cursor + 1 < end &&
            code[cursor + 1].isWhitespace()
    }

    private fun listMarkerEnd(start: Int, end: Int): Int {
        if (code[start] in "-*+") return start + 1
        var cursor = start
        while (cursor < end && code[cursor].isDigit()) cursor++
        return (cursor + 1).coerceAtMost(end)
    }

    private fun firstNonWhitespace(start: Int, end: Int): Int {
        var index = start
        while (index < end && code[index].isWhitespace()) index++
        return index
    }

    private fun lineEnd(start: Int, endExclusive: Int): Int {
        val next = code.indexOf('\n', start)
        return if (next == -1 || next > endExclusive) endExclusive else next
    }

    private fun nextLineStart(lineEnd: Int, endExclusive: Int): Int? =
        if (lineEnd < endExclusive && code[lineEnd] == '\n') lineEnd + 1 else null

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }
}

private data class FenceLine(
    val marker: Char,
    val length: Int,
    val markerStart: Int,
    val markerEnd: Int,
    val languageStart: Int?,
    val language: String?,
)

private data class MarkdownLineScanResult(
    val consumedEnd: Int? = null,
    val paragraphOpen: Boolean,
    val inTable: Boolean = false,
)

private data class MarkdownInlineSkipRange(
    val start: Int,
    val endExclusive: Int,
)

private val MarkdownHeadingRole = SyntaxRole.Markup.append("heading")
private val MarkdownTaskRole = SyntaxRole.Markup.append("task")
private val MarkdownTaskCheckedRole = MarkdownTaskRole.append("checked")
private val MarkdownTaskUncheckedRole = MarkdownTaskRole.append("unchecked")

private fun markdownHeadingRole(level: Int): SyntaxRole =
    MarkdownHeadingRole.append("h$level")

private fun parseFenceLanguage(info: String): String? {
    val trimmed = info.trim()
    if (trimmed.isEmpty()) return null
    if (trimmed.startsWith("{")) {
        val inner = trimmed.removePrefix("{").removeSuffix("}")
        return whitespaceSeparatedTokens(inner)
            .firstOrNull { it.startsWith(".") && it.length > 1 }
            ?.drop(1)
    }
    return firstWhitespaceSeparatedToken(trimmed)
}

private fun firstWhitespaceSeparatedToken(value: String): String? {
    var index = 0
    while (index < value.length && value[index].isWhitespace()) index++
    val start = index
    while (index < value.length && !value[index].isWhitespace()) index++
    return value.substring(start, index).takeIf { it.isNotBlank() }
}

private fun whitespaceSeparatedTokens(value: String): Sequence<String> = sequence {
    var index = 0
    while (index < value.length) {
        while (index < value.length && value[index].isWhitespace()) index++
        val start = index
        while (index < value.length && !value[index].isWhitespace()) index++
        if (index > start) {
            yield(value.substring(start, index))
        }
    }
}
