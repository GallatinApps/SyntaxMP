package com.gallatinapps.syntaxmp.builtins.dockerfile

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.appendEmbeddedSpans
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest

internal class DockerfileScanner(
    private val request: TokenizeRequest,
) {
    private val code: String = request.code
    private val language: LanguageId = request.languageId
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var lineStart = 0
        while (lineStart < code.length) {
            val lineEnd = lineEnd(lineStart)
            val scannedEnd = scanLine(lineStart, lineEnd)
            lineStart = when {
                scannedEnd > lineEnd -> scannedEnd
                lineEnd == code.length -> code.length
                else -> lineEnd + 1
            }
        }
        return tokens
    }

    private fun scanLine(start: Int, end: Int): Int {
        val first = firstNonWhitespace(start, end)
        if (first >= end) return end
        if (code[first] == '#') {
            add(first, end, SyntaxRole.Comment)
            return end
        }
        val instructionEnd = readWord(first, end)
        if (instructionEnd > first) {
            add(first, instructionEnd, SyntaxRole.Keyword)
            return scanArguments(
                start = instructionEnd,
                end = end,
                instruction = code.substring(first, instructionEnd).uppercase(),
            )
        }
        return end
    }

    private fun scanArguments(start: Int, end: Int, instruction: String): Int {
        var index = start
        while (index < end) {
            index = when {
                code[index].isWhitespace() -> index + 1
                code[index] == '#' -> {
                    add(index, end, SyntaxRole.Comment)
                    end
                }
                startsWith(index, "--") -> scanFlag(index, end)
                startsWith(index, "<<") -> {
                    val heredocEnd = scanHeredoc(
                        start = index,
                        argumentStart = start,
                        lineEnd = end,
                        instruction = instruction,
                    )
                    if (heredocEnd > end) return heredocEnd
                    heredocEnd
                }
                code[index] == '$' -> scanVariable(index, end)
                code[index] == '"' || code[index] == '\'' -> scanString(index, code[index], end)
                code[index].isDigit() -> scanNumber(index, end)
                code[index] in "[]=,:{}()" -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                code[index] in "+-*/\\" -> {
                    add(index, index + 1, SyntaxRole.Operator)
                    index + 1
                }
                else -> index + 1
            }
        }
        return end
    }

    private fun scanHeredoc(
        start: Int,
        argumentStart: Int,
        lineEnd: Int,
        instruction: String,
    ): Int {
        val header = parseHeredocHeader(start, lineEnd) ?: return start + 2
        val shellLanguageLabel = shellHeredocLanguageLabel(
            instruction = instruction,
            argumentStart = argumentStart,
            heredocStart = start,
            lineEnd = lineEnd,
            header = header,
        )
        var bodyLineStart = if (lineEnd < code.length) lineEnd + 1 else lineEnd
        while (bodyLineStart < code.length) {
            val bodyLineEnd = lineEnd(bodyLineStart)
            val closingStart = closingDelimiterStart(bodyLineStart, bodyLineEnd, header.delimiter)
            if (closingStart != null) {
                emitHeredoc(
                    start = start,
                    headerLineEnd = lineEnd,
                    bodyStart = if (lineEnd < code.length) lineEnd + 1 else lineEnd,
                    bodyEnd = bodyLineStart,
                    closingStart = bodyLineStart,
                    closingEnd = bodyLineEnd,
                    shellLanguageLabel = shellLanguageLabel,
                )
                return bodyLineEnd
            }
            bodyLineStart = if (bodyLineEnd < code.length) bodyLineEnd + 1 else code.length
        }
        emitHeredoc(
            start = start,
            headerLineEnd = lineEnd,
            bodyStart = if (lineEnd < code.length) lineEnd + 1 else lineEnd,
            bodyEnd = code.length,
            closingStart = code.length,
            closingEnd = code.length,
            shellLanguageLabel = shellLanguageLabel,
        )
        return code.length
    }

    private fun emitHeredoc(
        start: Int,
        headerLineEnd: Int,
        bodyStart: Int,
        bodyEnd: Int,
        closingStart: Int,
        closingEnd: Int,
        shellLanguageLabel: String?,
    ) {
        if (shellLanguageLabel == null) {
            add(start, closingEnd, SyntaxRole.String)
            return
        }
        add(start, headerLineEnd, SyntaxRole.String)
        tokens.appendEmbeddedSpans(
            parentCode = request.code,
            bodyStart = bodyStart,
            bodyEnd = bodyEnd,
            languageLabel = shellLanguageLabel,
            tokenizeEmbedded = request::tokenizeEmbedded,
        )
        add(closingStart, closingEnd, SyntaxRole.String)
    }

    private fun parseHeredocHeader(start: Int, lineEnd: Int): HeredocHeader? {
        var cursor = start + 2
        if (code.getOrNull(cursor) == '-') cursor++
        while (cursor < lineEnd && code[cursor] in " \t") cursor++
        val quote = code.getOrNull(cursor)?.takeIf { it == '\'' || it == '"' }
        if (quote != null) cursor++
        val delimiterStart = cursor
        if (quote != null) {
            while (cursor < lineEnd && code[cursor] != quote) cursor++
            if (cursor >= lineEnd) return null
        } else {
            while (cursor < lineEnd && (code[cursor].isLetterOrDigit() || code[cursor] in "_-")) cursor++
        }
        if (cursor == delimiterStart) return null
        val delimiter = code.substring(delimiterStart, cursor)
        val afterDelimiter = if (quote != null) cursor + 1 else cursor
        return HeredocHeader(
            delimiter = delimiter,
            afterDelimiter = afterDelimiter,
        )
    }

    private fun shellHeredocLanguageLabel(
        instruction: String,
        argumentStart: Int,
        heredocStart: Int,
        lineEnd: Int,
        header: HeredocHeader,
    ): String? {
        if (instruction != "RUN") return null
        val before = commandTokens(argumentStart, heredocStart)
        val after = commandTokens(header.afterDelimiter, lineEnd)
        return when {
            before.isEmpty() && after.isEmpty() -> LanguageId.Shell.value
            before.isEmpty() -> after.singleOrNull()?.toShellLanguageLabel()
            after.isEmpty() -> before.singleOrNull()?.toShellLanguageLabel()
            else -> null
        }
    }

    private fun commandTokens(start: Int, end: Int): List<String> {
        val result = mutableListOf<String>()
        var cursor = start
        while (cursor < end) {
            while (cursor < end && code[cursor].isWhitespace()) cursor++
            if (cursor >= end || code[cursor] == '#') break
            val quote = code[cursor].takeIf { it == '\'' || it == '"' }
            val tokenStart = if (quote == null) cursor else cursor + 1
            cursor = tokenStart
            if (quote != null) {
                while (cursor < end && code[cursor] != quote) cursor++
                if (cursor > tokenStart) result += code.substring(tokenStart, cursor)
                if (cursor < end) cursor++
            } else {
                while (cursor < end && !code[cursor].isWhitespace()) cursor++
                if (cursor > tokenStart) result += code.substring(tokenStart, cursor)
            }
        }
        return result
    }

    private fun closingDelimiterStart(start: Int, end: Int, delimiter: String): Int? {
        val first = firstNonWhitespace(start, end)
        return if (startsWith(first, delimiter) && first + delimiter.length == end) first else null
    }

    private fun scanFlag(start: Int, limit: Int): Int {
        var end = start + 2
        while (end < limit && (code[end].isLetterOrDigit() || code[end] == '-' || code[end] == '_')) end++
        add(start, end, SyntaxRole.Attribute)
        return end
    }

    private fun scanVariable(start: Int, limit: Int): Int {
        var end = start + 1
        if (end < limit && code[end] == '{') {
            end++
            while (end < limit && code[end] != '}') end++
            if (end < limit) end++
        } else {
            while (end < limit && (code[end].isLetterOrDigit() || code[end] == '_')) end++
        }
        add(start, end, SyntaxRole.Variable)
        return end
    }

    private fun scanString(start: Int, quote: Char, limit: Int): Int {
        var end = start + 1
        while (end < limit) {
            if (quote == '"' && code[end] == '\\') end += 2
            else if (code[end] == quote) {
                add(start, end + 1, SyntaxRole.String)
                return end + 1
            } else {
                end++
            }
        }
        add(start, limit, SyntaxRole.String)
        return limit
    }

    private fun scanNumber(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && (code[end].isDigit() || code[end] == '.')) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun readWord(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && code[end].isLetter()) end++
        return end
    }

    private fun firstNonWhitespace(start: Int, end: Int): Int {
        var index = start
        while (index < end && code[index].isWhitespace()) index++
        return index
    }

    private fun lineEnd(start: Int): Int =
        code.indexOf('\n', start).let { if (it == -1) code.length else it }

    private fun startsWith(index: Int, value: String): Boolean =
        index + value.length <= code.length && code.regionMatches(index, value, 0, value.length)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }

    private fun String.toShellLanguageLabel(): String? =
        when (this) {
            "sh" -> LanguageId.Shell.value
            "bash" -> LanguageId.Bash.value
            else -> null
        }

    private data class HeredocHeader(
        val delimiter: String,
        val afterDelimiter: Int,
    )
}
