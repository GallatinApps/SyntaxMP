package com.gallatinapps.syntaxmp.languages.hcl

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal class HclScanner(
    private val code: String,
    private val language: SyntaxLanguageId,
    private val constants: Set<String> = emptySet(),
    private val declarationKeywordRoles: Map<String, SyntaxRole> = emptyMap(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        scanRange(start = 0, limit = code.length)
        return tokens
    }

    private fun scanRange(start: Int, limit: Int) {
        var index = start
        while (index < limit) {
            index = when {
                code[index].isWhitespace() -> index + 1
                startsWith(index, "//") || code[index] == '#' -> scanLineComment(index)
                startsWith(index, "/*") -> scanBlockComment(index)
                code[index] == '"' -> scanString(index)
                code[index] == '$' -> scanInterpolationMarker(index)
                code[index] == '-' || code[index] == '+' || code[index].isDigit() -> scanNumber(index)
                code[index].isHclIdentifierStart() -> scanIdentifier(index)
                code[index] in "{}[](),." -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                code[index] in "+-*/%=!<>?:" -> scanOperator(index)
                else -> index + 1
            }
        }
    }

    private fun scanLineComment(start: Int): Int {
        val end = code.indexOf('\n', start).let { if (it == -1) code.length else it }
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanBlockComment(start: Int): Int {
        val end = code.indexOf("*/", start + 2).let { if (it == -1) code.length else it + 2 }
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanString(start: Int): Int {
        var index = start + 1
        var segmentStart = start
        while (index < code.length) {
            when {
                code[index] == '\\' -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    val escapeEnd = (index + 2).coerceAtMost(code.length)
                    add(index, escapeEnd, SyntaxRole.Escape)
                    index = escapeEnd
                    segmentStart = index
                }
                startsWith(index, "\${") -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = scanInterpolation(index) ?: run {
                        add(index, index + 2, SyntaxRole.Escape)
                        index + 2
                    }
                    segmentStart = index
                }
                code[index] == '"' -> {
                    val end = index + 1
                    if (segmentStart < end) add(segmentStart, end, SyntaxRole.String)
                    return end
                }
                else -> index++
            }
        }
        if (segmentStart < code.length) add(segmentStart, code.length, SyntaxRole.String)
        return code.length
    }

    private fun scanInterpolationMarker(start: Int): Int {
        return scanInterpolation(start) ?: if (startsWith(start, "\${")) {
            add(start, start + 2, SyntaxRole.Escape)
            start + 2
        } else {
            start + 1
        }
    }

    private fun scanInterpolation(start: Int): Int? {
        if (!startsWith(start, "\${")) return null
        val close = findInterpolationClose(start) ?: return null
        add(start, start + 2, SyntaxRole.Escape)
        scanRange(start = start + 2, limit = close)
        add(close, close + 1, SyntaxRole.Escape)
        return close + 1
    }

    private fun findInterpolationClose(start: Int): Int? {
        var depth = 1
        var index = start + 2
        while (index < code.length) {
            index = when {
                code[index] == '"' -> skipString(index)
                startsWith(index, "\${") -> {
                    depth++
                    index + 2
                }
                code[index] == '{' -> {
                    depth++
                    index + 1
                }
                code[index] == '}' -> {
                    depth--
                    if (depth == 0) return index
                    index + 1
                }
                else -> index + 1
            }
        }
        return null
    }

    private fun scanNumber(start: Int): Int {
        var end = start
        if (code[end] == '-' || code[end] == '+') end++
        while (end < code.length && code[end].isDigit()) end++
        if (end < code.length && code[end] == '.') {
            end++
            while (end < code.length && code[end].isDigit()) end++
        }
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanIdentifier(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isHclIdentifierPart()) end++
        val word = code.substring(start, end)
        val declarationRole = declarationKeywordRoles[word]
        val role = when {
            previousNonWhitespace(start) == '.' -> SyntaxRole.Property
            declarationRole != null -> declarationRole
            word in constants -> SyntaxRole.Constant.Builtin.append(word)
            nextNonWhitespace(end) == '=' -> SyntaxRole.Property
            nextNonWhitespace(end) == '{' || hasBlockOpenAfterLabels(end) -> SyntaxRole.Type
            nextNonWhitespace(end) == '(' -> SyntaxRole.Function
            else -> SyntaxRole.Variable
        }
        add(start, end, role)
        return end
    }

    private fun scanOperator(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end] in "+-*/%=!<>?:") end++
        add(start, end, SyntaxRole.Operator)
        return end
    }

    private fun previousNonWhitespace(index: Int): Char? {
        var cursor = index - 1
        while (cursor >= 0 && code[cursor].isWhitespace()) cursor--
        return code.getOrNull(cursor)
    }

    private fun nextNonWhitespace(index: Int): Char? {
        var cursor = index
        while (cursor < code.length && code[cursor].isWhitespace()) cursor++
        return code.getOrNull(cursor)
    }

    private fun hasBlockOpenAfterLabels(index: Int): Boolean {
        var cursor = index
        while (true) {
            cursor = firstNonWhitespace(cursor)
            if (code.getOrNull(cursor) == '{') return true
            if (code.getOrNull(cursor) != '"') return false
            cursor = skipString(cursor)
        }
    }

    private fun firstNonWhitespace(index: Int): Int {
        var cursor = index
        while (cursor < code.length && code[cursor].isWhitespace()) cursor++
        return cursor
    }

    private fun skipString(start: Int): Int {
        var cursor = start + 1
        while (cursor < code.length) {
            if (code[cursor] == '\\') {
                cursor += 2
            } else if (code[cursor] == '"') {
                return cursor + 1
            } else {
                cursor++
            }
        }
        return code.length
    }

    private fun startsWith(index: Int, value: String): Boolean =
        index + value.length <= code.length && code.regionMatches(index, value, 0, value.length)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }
}

private fun Char.isHclIdentifierStart(): Boolean =
    this == '_' || isLetter()

private fun Char.isHclIdentifierPart(): Boolean =
    isHclIdentifierStart() || isDigit() || this == '-'
