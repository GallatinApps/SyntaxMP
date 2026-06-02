package com.gallatinapps.syntaxmp.builtins.makefile

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal class MakefileScanner(
    private val code: String,
    private val language: LanguageId,
    private val directiveRoles: LexemeRoleMap = emptyMap(),
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
        if (code[first] == '#') {
            add(first, end, SyntaxRole.Comment)
            return
        }
        if (code[start] == '\t') {
            scanRecipe(first, end)
            return
        }
        val wordEnd = readName(first, end)
        val directive = code.substring(first, wordEnd)
        val directiveRole = directiveRoles[directive]
        if (directiveRole != null) {
            add(first, wordEnd, directiveRole)
            scanRecipe(wordEnd, end)
            return
        }
        val separator = findAssignmentOrTarget(first, end)
        if (separator != -1) {
            val role = if (code[separator] == ':' && code.getOrNull(separator + 1) != '=') {
                SyntaxRole.Function
            } else {
                SyntaxRole.Property
            }
            add(first, trimTrailingWhitespace(first, separator), role)
            val operatorEnd = if (separator + 1 < end && code[separator + 1] in "=:+?") separator + 2 else separator + 1
            add(separator, operatorEnd, SyntaxRole.Operator)
            scanRecipe(operatorEnd, end)
        }
    }

    private fun scanRecipe(start: Int, end: Int) {
        var index = start
        var firstWord = true
        while (index < end) {
            index = when {
                code[index].isWhitespace() -> index + 1
                code[index] == '#' -> {
                    add(index, end, SyntaxRole.Comment)
                    end
                }
                code[index] == '$' -> scanVariable(index, end)
                code[index] == '"' || code[index] == '\'' -> scanString(index, code[index], end)
                code[index].isDigit() -> scanNumber(index, end)
                code[index].isLetter() || code[index] in "_./-" -> {
                    val next = readCommandWord(index, end)
                    if (firstWord) add(index, next, SyntaxRole.Function)
                    firstWord = false
                    next
                }
                code[index] in "():=+-*/\\@" -> {
                    add(index, index + 1, SyntaxRole.Operator)
                    index + 1
                }
                else -> index + 1
            }
        }
    }

    private fun scanVariable(start: Int, limit: Int): Int {
        var end = start + 1
        if (end < limit && code[end] in "({") {
            val close = if (code[end] == '(') ')' else '}'
            end++
            while (end < limit && code[end] != close) end++
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

    private fun findAssignmentOrTarget(start: Int, end: Int): Int {
        var index = start
        while (index < end) {
            if (code[index] == ':' || code[index] == '=') return index
            index++
        }
        return -1
    }

    private fun readName(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && (code[end].isLetterOrDigit() || code[end] in "_-.")) end++
        return end
    }

    private fun readCommandWord(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && !code[end].isWhitespace() && code[end] !in "#$(){}") end++
        return end
    }

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
