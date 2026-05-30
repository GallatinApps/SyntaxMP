package com.gallatinapps.syntaxmp.languages.r

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal class RScanner(
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
                code[index] == '#' -> scanLineComment(index)
                code[index] == '"' || code[index] == '\'' -> scanString(index, code[index])
                code[index].isDigit() -> scanNumber(index)
                code[index] == '`' -> scanBacktickName(index)
                code[index].isRIdentifierStart() || code[index] == '.' -> scanIdentifier(index)
                code[index] in "{}[](),.;" -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                code[index] in "+-*/%=!<>|&~:$" -> scanOperator(index)
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

    private fun scanBacktickName(start: Int): Int {
        val end = code.indexOf('`', start + 1).let { if (it == -1) code.length else it + 1 }
        add(start, end, SyntaxRole.Property)
        return end
    }

    private fun scanNumber(start: Int): Int {
        var end = start
        while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '.')) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanIdentifier(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isRIdentifierPart()) end++
        val word = code.substring(start, end)
        val role = when {
            word in constants -> SyntaxRole.Constant.Builtin.append(word)
            word in keywordRoles -> keywordRoles.getValue(word)
            word in builtinRoles -> builtinRoles.getValue(word)
            nextNonWhitespace(end) == '(' -> SyntaxRole.Function
            previousNonWhitespace(start) == '$' -> SyntaxRole.Property
            else -> SyntaxRole.Variable
        }
        add(start, end, role)
        return end
    }

    private fun scanOperator(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end] in "+-*/%=!<>|&~:$") end++
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

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }

}

private fun Char.isRIdentifierStart(): Boolean =
    this == '_' || isLetter()

private fun Char.isRIdentifierPart(): Boolean =
    isRIdentifierStart() || isDigit() || this == '.'
