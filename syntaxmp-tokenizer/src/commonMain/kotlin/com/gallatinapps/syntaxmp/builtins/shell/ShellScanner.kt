package com.gallatinapps.syntaxmp.builtins.shell

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.primitives.findBraceBalancedEnd
import com.gallatinapps.syntaxmp.primitives.isIdentifierPart
import com.gallatinapps.syntaxmp.primitives.isIdentifierStart

internal class ShellScanner(
    private val code: String,
    private val language: LanguageId,
    private val keywordRoles: LexemeRoleMap = emptyMap(),
    private val builtinRoles: LexemeRoleMap = emptyMap(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        scanRange(start = 0, limit = code.length)
        return tokens
    }

    private fun scanRange(start: Int, limit: Int) {
        var index = start
        while (index < limit) {
            val char = code[index]
            index = when {
                char.isWhitespace() -> index + 1
                char == '#' -> scanComment(index, limit)
                startsWith(index, "<<", limit) -> scanHeredoc(index, limit)
                char == '"' || char == '\'' -> scanString(index, char, limit)
                char == '`' -> scanBacktickCommand(index, limit)
                char == '$' -> scanVariable(index, limit)
                char.isDigit() -> scanNumber(index, limit)
                char.isIdentifierStart() -> scanWord(index, limit)
                char in "|&;(){}[]" -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                char in "-=+*/<>!" -> scanOperator(index, limit)
                else -> index + 1
            }
        }
    }

    private fun scanComment(start: Int, limit: Int): Int {
        val end = code.indexOf('\n', start).let { if (it == -1) code.length else it }
            .coerceAtMost(limit)
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanString(start: Int, quote: Char, limit: Int): Int {
        var index = start + 1
        var segmentStart = start
        while (index < limit) {
            when {
                quote == '"' && startsWith(index, "$(", limit) -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = scanCommandSubstitution(index, limit)
                    segmentStart = index
                }
                quote == '"' && code[index] == '`' -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = scanBacktickCommand(index, limit)
                    segmentStart = index
                }
                quote == '"' && code[index] == '$' -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = scanVariable(index, limit)
                    segmentStart = index
                }
                quote == '"' && code[index] == '\\' -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    val escapeEnd = (index + 2).coerceAtMost(limit)
                    add(index, escapeEnd, SyntaxRole.Escape)
                    index = escapeEnd
                    segmentStart = index
                }
                code[index] == '\\' -> index += 2
                code[index] == quote -> {
                    val end = index + 1
                    if (segmentStart < end) add(segmentStart, end, SyntaxRole.String)
                    return end
                }
                else -> index++
            }
        }
        if (segmentStart < limit) add(segmentStart, limit, SyntaxRole.String)
        return limit
    }

    private fun scanHeredoc(start: Int, limit: Int): Int {
        var cursor = start + 2
        val allowIndentedClosing = code.getOrNull(cursor) == '-'
        if (allowIndentedClosing) cursor++
        while (cursor < limit && code[cursor] in " \t") cursor++
        val quote = code.getOrNull(cursor)?.takeIf { it == '\'' || it == '"' }
        if (quote != null) cursor++
        val delimiterStart = cursor
        if (quote != null) {
            while (cursor < limit && code[cursor] != quote && code[cursor] != '\n') cursor++
            if (cursor >= limit || code[cursor] != quote) return scanOperator(start, limit)
        } else {
            while (cursor < limit && (code[cursor].isIdentifierPart() || code[cursor] in "_-")) cursor++
        }
        if (cursor == delimiterStart) return scanOperator(start, limit)
        val delimiter = code.substring(delimiterStart, cursor)
        val headerEnd = lineEnd(cursor, limit)
        var lineStart = if (headerEnd < limit) headerEnd + 1 else headerEnd
        while (lineStart < limit) {
            val lineEnd = lineEnd(lineStart, limit)
            var closeCursor = lineStart
            if (allowIndentedClosing) {
                while (closeCursor < lineEnd && code[closeCursor] == '\t') closeCursor++
            }
            if (startsWith(closeCursor, delimiter, limit)) {
                var rest = closeCursor + delimiter.length
                while (rest < lineEnd && code[rest] in " \t") rest++
                if (rest == lineEnd) {
                    add(start, lineEnd, SyntaxRole.String)
                    return lineEnd
                }
            }
            lineStart = if (lineEnd < limit) lineEnd + 1 else limit
        }
        add(start, limit, SyntaxRole.String)
        return limit
    }

    private fun scanCommandSubstitution(start: Int, limit: Int): Int {
        if (startsWith(start, "$(", limit) && code.getOrNull(start + 2) == '(') {
            val expressionStart = start + 3
            val expressionEnd = code.indexOf("))", expressionStart).let { if (it == -1) limit else it.coerceAtMost(limit) }
            add(start, expressionStart, SyntaxRole.Escape)
            scanRange(start = expressionStart, limit = expressionEnd)
            if (expressionEnd + 1 < limit) {
                add(expressionEnd, expressionEnd + 2, SyntaxRole.Escape)
                return expressionEnd + 2
            }
            return expressionEnd
        }
        val expressionStart = start + 2
        val expressionEnd = findBraceBalancedEnd(
            code = code,
            openIndex = start + 1,
            openBrace = '(',
            closeBrace = ')',
        ).coerceAtMost(limit)
        add(start, expressionStart, SyntaxRole.Escape)
        scanRange(start = expressionStart, limit = expressionEnd)
        if (expressionEnd < limit && code[expressionEnd] == ')') {
            add(expressionEnd, expressionEnd + 1, SyntaxRole.Escape)
            return expressionEnd + 1
        }
        return expressionEnd
    }

    private fun scanBacktickCommand(start: Int, limit: Int): Int {
        add(start, start + 1, SyntaxRole.Escape)
        var index = start + 1
        while (index < limit) {
            when {
                code[index] == '\\' -> index += 2
                code[index] == '`' -> {
                    scanRange(start = start + 1, limit = index)
                    add(index, index + 1, SyntaxRole.Escape)
                    return index + 1
                }
                else -> index++
            }
        }
        scanRange(start = start + 1, limit = limit)
        return limit
    }

    private fun scanVariable(start: Int, limit: Int): Int {
        if (startsWith(start, "$(", limit)) return scanCommandSubstitution(start, limit)
        var end = start + 1
        if (end < limit && code[end] == '{') {
            val expressionStart = end + 1
            val expressionEnd = findBraceBalancedEnd(code = code, openIndex = end).coerceAtMost(limit)
            add(start, expressionStart, SyntaxRole.Escape)
            scanRange(start = expressionStart, limit = expressionEnd)
            if (expressionEnd < limit && code[expressionEnd] == '}') {
                add(expressionEnd, expressionEnd + 1, SyntaxRole.Escape)
                return expressionEnd + 1
            }
            return expressionEnd
        } else {
            while (end < limit && code[end].isIdentifierPart()) end++
        }
        add(start, end, SyntaxRole.Variable.Parameter)
        return end
    }

    private fun lineEnd(start: Int, limit: Int): Int =
        code.indexOf('\n', start).let { if (it == -1) code.length else it }.coerceAtMost(limit)

    private fun scanNumber(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && code[end].isDigit()) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanWord(start: Int, limit: Int): Int {
        var end = start + 1
        while (end < limit && (code[end].isIdentifierPart() || code[end] == '-')) end++
        val word = code.substring(start, end)
        val role = when {
            word in keywordRoles -> keywordRoles.getValue(word)
            word in builtinRoles -> builtinRoles.getValue(word)
            else -> SyntaxRole.Variable
        }
        add(start, end, role)
        return end
    }

    private fun scanOperator(start: Int, limit: Int): Int {
        var end = start + 1
        while (end < limit && code[end] in "-=+*/<>!") end++
        add(start, end, SyntaxRole.Operator)
        return end
    }

    private fun startsWith(index: Int, value: String, limit: Int): Boolean =
        index + value.length <= limit && code.regionMatches(index, value, 0, value.length)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }
}
