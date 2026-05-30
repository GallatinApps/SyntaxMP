package com.gallatinapps.syntaxmp.languages.ini

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

private const val ExportLineKeyword = "export"

internal class IniScanner(
    private val code: String,
    private val language: SyntaxLanguageId,
    private val constants: Set<String> = emptySet(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var lineStart = 0
        while (lineStart <= code.length) {
            val lineEnd = code.indexOf('\n', lineStart).let { if (it == -1) code.length else it }
            scanLine(lineStart, lineEnd)
            if (lineEnd == code.length) break
            lineStart = lineEnd + 1
        }
        return tokens
    }

    private fun scanLine(start: Int, end: Int) {
        val first = firstNonWhitespace(start, end)
        if (first >= end) return
        if (code[first] == '#' || code[first] == ';') {
            add(first, end, SyntaxRole.Comment)
            return
        }
        if (code[first] == '[') {
            scanSection(first, end)
            return
        }
        var keyStart = first
        if (startsWith(first, ExportLineKeyword) && isBoundary(first + ExportLineKeyword.length, end)) {
            add(first, first + ExportLineKeyword.length, SyntaxRole.Keyword)
            keyStart = firstNonWhitespace(first + ExportLineKeyword.length, end)
        }
        val separator = findSeparator(keyStart, end)
        if (separator != -1) {
            add(keyStart, trimTrailingWhitespace(keyStart, separator), SyntaxRole.Property)
            add(separator, separator + 1, SyntaxRole.Operator)
            scanValue(separator + 1, end)
        }
    }

    private fun scanSection(start: Int, end: Int) {
        add(start, start + 1, SyntaxRole.Punctuation)
        val close = code.indexOf(']', start + 1).let { if (it == -1 || it > end) end else it }
        add(start + 1, close, SyntaxRole.Property.Section)
        if (close < end) add(close, close + 1, SyntaxRole.Punctuation)
    }

    private fun scanValue(start: Int, end: Int) {
        var index = firstNonWhitespace(start, end)
        while (index < end) {
            index = when {
                code[index] == '#' || code[index] == ';' -> {
                    add(index, end, SyntaxRole.Comment)
                    end
                }
                code[index] == '"' || code[index] == '\'' -> scanString(index, code[index], end)
                code[index].isDigit() || code[index] == '-' || code[index] == '+' -> scanNumber(index, end)
                code[index].isLetter() -> scanWord(index, end)
                code[index] in "[],." -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                else -> index + 1
            }
        }
    }

    private fun scanString(start: Int, quote: Char, limit: Int): Int {
        var index = start + 1
        while (index < limit) {
            if (quote == '"' && code[index] == '\\') index += 2
            else if (code[index] == quote) {
                add(start, index + 1, SyntaxRole.String)
                return index + 1
            } else {
                index++
            }
        }
        add(start, limit, SyntaxRole.String)
        return limit
    }

    private fun scanNumber(start: Int, limit: Int): Int {
        var end = start
        if (code[end] == '-' || code[end] == '+') end++
        while (end < limit && (code[end].isLetterOrDigit() || code[end] == '_' || code[end] == '.')) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanWord(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && (code[end].isLetterOrDigit() || code[end] == '_' || code[end] == '-' || code[end] == '.')) end++
        val word = code.substring(start, end).lowercase()
        val role = when (word) {
            in constants ->
                SyntaxRole.Constant.Builtin.append(word)
            else -> SyntaxRole.String
        }
        add(start, end, role)
        return end
    }

    private fun findSeparator(start: Int, end: Int): Int {
        var index = start
        while (index < end) {
            if (code[index] == '=' || code[index] == ':') return index
            index++
        }
        return -1
    }

    private fun startsWith(index: Int, value: String): Boolean =
        index + value.length <= code.length && code.regionMatches(index, value, 0, value.length)

    private fun isBoundary(index: Int, end: Int): Boolean =
        index >= end || code[index].isWhitespace()

    private fun firstNonWhitespace(start: Int, end: Int): Int {
        var index = start
        while (index < end && code[index].isWhitespace()) index++
        return index
    }

    private fun trimTrailingWhitespace(start: Int, end: Int): Int {
        var index = end
        while (index > start && code[index - 1].isWhitespace()) index--
        return index
    }

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }
}
