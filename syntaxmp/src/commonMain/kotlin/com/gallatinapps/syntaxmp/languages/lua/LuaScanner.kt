package com.gallatinapps.syntaxmp.languages.lua

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierPart
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierStart

internal class LuaScanner(
    private val code: String,
    private val language: SyntaxLanguageId,
    private val keywordRoles: LexemeRoleMap = emptyMap(),
    private val constants: Set<String> = emptySet(),
    private val builtinRoles: LexemeRoleMap = emptyMap(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var index = 0
        while (index < code.length) {
            index = when {
                code[index].isWhitespace() -> index + 1
                startsWith(index, "--[") && longBracketClose(index + 2) != null -> scanLongComment(index)
                startsWith(index, "--") -> scanLineComment(index)
                code[index] == '[' && longBracketClose(index) != null -> scanLongString(index)
                code[index] == '"' || code[index] == '\'' -> scanString(index, code[index])
                code[index].isDigit() -> scanNumber(index)
                code[index].isIdentifierStart() -> scanIdentifier(index)
                code[index] in "{}[](),.;:" -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                code[index] in "+-*/%=!<>#~^" -> scanOperator(index)
                else -> index + 1
            }
        }
        return tokens
    }

    private fun scanLineComment(start: Int): Int {
        val end = code.indexOf('\n', start).let { if (it == -1) code.length else it }
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanLongComment(start: Int): Int {
        val bracketStart = start + 2
        val close = longBracketClose(bracketStart) ?: "]]"
        val bodyStart = bracketStart + close.length
        val end = code.indexOf(close, bodyStart).let { if (it == -1) code.length else it + close.length }
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanLongString(start: Int): Int {
        val close = longBracketClose(start) ?: "]]"
        val bodyStart = start + close.length
        val end = code.indexOf(close, bodyStart).let { if (it == -1) code.length else it + close.length }
        add(start, end, SyntaxRole.String)
        return end
    }

    private fun longBracketClose(start: Int): String? {
        if (code.getOrNull(start) != '[') return null
        var index = start + 1
        while (code.getOrNull(index) == '=') index++
        if (code.getOrNull(index) != '[') return null
        return "]" + "=".repeat(index - start - 1) + "]"
    }

    private fun scanString(start: Int, quote: Char): Int {
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
                code[index] == quote -> {
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

    private fun scanNumber(start: Int): Int {
        var end = start
        while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '.')) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanIdentifier(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isIdentifierPart()) end++
        val word = code.substring(start, end)
        val role = when {
            word in constants -> SyntaxRole.Constant.Builtin.append(word)
            word in keywordRoles -> keywordRoles.getValue(word)
            word in builtinRoles -> builtinRoles.getValue(word)
            nextNonWhitespace(end) == '(' -> SyntaxRole.Function
            previousNonWhitespace(start) == '.' || previousNonWhitespace(start) == ':' -> SyntaxRole.Property
            else -> SyntaxRole.Variable
        }
        add(start, end, role)
        return end
    }

    private fun scanOperator(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end] in "+-*/%=!<>#~^") end++
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

    private fun startsWith(index: Int, value: String): Boolean =
        index + value.length <= code.length && code.regionMatches(index, value, 0, value.length)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }

}
