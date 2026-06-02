package com.gallatinapps.syntaxmp.builtins.graphql

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal class GraphQlScanner(
    private val code: String,
    private val language: LanguageId,
    private val keywordRoles: LexemeRoleMap = emptyMap(),
    private val constants: Set<String> = emptySet(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var index = 0
        while (index < code.length) {
            index = when {
                code[index].isWhitespace() -> index + 1
                code[index] == '#' -> scanLineComment(index)
                startsWith(index, "\"\"\"") -> scanBlockString(index)
                code[index] == '"' -> scanString(index)
                code[index] == '$' -> scanVariable(index)
                code[index] == '@' -> scanDirective(index)
                code[index] == '-' || code[index].isDigit() -> scanNumber(index)
                code[index].isGraphQlIdentifierStart() -> scanIdentifier(index)
                code[index] in "{}[]():=!|&." -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
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

    private fun scanBlockString(start: Int): Int {
        val end = code.indexOf("\"\"\"", start + 3).let { if (it == -1) code.length else it + 3 }
        add(start, end, SyntaxRole.String)
        return end
    }

    private fun scanString(start: Int): Int {
        var end = start + 1
        while (end < code.length) {
            if (code[end] == '\\') end += 2
            else if (code[end] == '"') {
                add(start, end + 1, SyntaxRole.String)
                return end + 1
            } else {
                end++
            }
        }
        add(start, code.length, SyntaxRole.String)
        return code.length
    }

    private fun scanVariable(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isGraphQlIdentifierPart()) end++
        add(start, end, SyntaxRole.Variable.Parameter)
        return end
    }

    private fun scanDirective(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isGraphQlIdentifierPart()) end++
        add(start, end, SyntaxRole.Annotation)
        return end
    }

    private fun scanNumber(start: Int): Int {
        var end = start
        if (code[end] == '-') end++
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
        while (end < code.length && code[end].isGraphQlIdentifierPart()) end++
        val word = code.substring(start, end)
        val role = when {
            word in keywordRoles -> keywordRoles.getValue(word)
            word in constants -> SyntaxRole.Constant.Builtin.append(word)
            word.first().isUpperCase() -> SyntaxRole.Type
            previousNonWhitespace(start) == ':' -> SyntaxRole.Type
            nextNonWhitespace(end) == '(' || nextNonWhitespace(end) == '{' -> SyntaxRole.Function
            else -> SyntaxRole.Property
        }
        add(start, end, role)
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

private fun Char.isGraphQlIdentifierStart(): Boolean =
    this == '_' || isLetter()

private fun Char.isGraphQlIdentifierPart(): Boolean =
    isGraphQlIdentifierStart() || isDigit()
