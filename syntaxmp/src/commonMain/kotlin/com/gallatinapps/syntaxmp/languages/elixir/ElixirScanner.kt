package com.gallatinapps.syntaxmp.languages.elixir

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierPart
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierStart

internal class ElixirScanner(
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
                startsWith(index, "\"\"\"") || startsWith(index, "'''") -> scanTripleString(index)
                code[index] == '"' || code[index] == '\'' -> scanString(index, code[index])
                code[index] == '~' -> scanSigil(index) ?: scanOperator(index)
                code[index] == ':' -> scanAtom(index)
                code[index] == '@' -> scanAttribute(index)
                code[index].isDigit() -> scanNumber(index)
                code[index].isIdentifierStart() -> scanIdentifier(index)
                code[index] in "{}[](),.;" -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                code[index] in "+-*/%=!<>|&^~?:" -> scanOperator(index)
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

    private fun scanTripleString(start: Int): Int {
        val quote = code.substring(start, start + 3)
        return scanDelimitedString(
            start = start,
            contentStart = start + 3,
            close = quote,
            interpolationEnabled = true,
            escapesEnabled = true,
        )
    }

    private fun scanString(start: Int, quote: Char): Int =
        scanDelimitedString(
            start = start,
            contentStart = start + 1,
            close = quote.toString(),
            interpolationEnabled = true,
            escapesEnabled = true,
        )

    private fun scanSigil(start: Int): Int? {
        val sigil = code.getOrNull(start + 1)?.takeIf { it.isLetter() } ?: return null
        val delimiterStart = start + 2
        val close = sigilCloseDelimiter(code.getOrNull(delimiterStart) ?: return null) ?: return null
        val interpolationEnabled = sigil.isLowerCase()
        return scanDelimitedString(
            start = start,
            contentStart = delimiterStart + 1,
            close = close.toString(),
            interpolationEnabled = interpolationEnabled,
            escapesEnabled = interpolationEnabled,
            includeTrailingLetters = true,
        )
    }

    private fun sigilCloseDelimiter(open: Char): Char? =
        when (open) {
            '(' -> ')'
            '[' -> ']'
            '{' -> '}'
            '<' -> '>'
            '/', '|', '"', '\'', '`' -> open
            else -> null
        }

    private fun scanDelimitedString(
        start: Int,
        contentStart: Int,
        close: String,
        interpolationEnabled: Boolean,
        escapesEnabled: Boolean,
        includeTrailingLetters: Boolean = false,
    ): Int {
        var index = contentStart
        var segmentStart = start
        while (index < code.length) {
            when {
                interpolationEnabled && startsWith(index, "#{") -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    add(index, index + 2, SyntaxRole.Escape)
                    index += 2
                    segmentStart = index
                }
                escapesEnabled && code[index] == '\\' -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    val escapeEnd = (index + 2).coerceAtMost(code.length)
                    add(index, escapeEnd, SyntaxRole.Escape)
                    index = escapeEnd
                    segmentStart = index
                }
                startsWith(index, close) -> {
                    var end = index + close.length
                    if (includeTrailingLetters) {
                        while (end < code.length && code[end].isLetter()) end++
                    }
                    if (segmentStart < end) add(segmentStart, end, SyntaxRole.String)
                    return end
                }
                else -> index++
            }
        }
        if (segmentStart < code.length) add(segmentStart, code.length, SyntaxRole.String)
        return code.length
    }

    private fun scanAtom(start: Int): Int {
        var end = start + 1
        while (end < code.length && (code[end].isIdentifierPart() || code[end] in "!?")) end++
        if (end > start + 1) {
            add(start, end, SyntaxRole.Constant.Atom)
            return end
        }
        add(start, start + 1, SyntaxRole.Punctuation)
        return start + 1
    }

    private fun scanAttribute(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isIdentifierPart()) end++
        add(start, end, SyntaxRole.Annotation)
        return end
    }

    private fun scanNumber(start: Int): Int {
        var end = start
        while (end < code.length && (code[end].isLetterOrDigit() || code[end] == '_' || code[end] == '.')) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanIdentifier(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isIdentifierPart()) end++
        if (end < code.length && code[end] in "!?") end++
        val word = code.substring(start, end)
        val role = when {
            word in constants -> SyntaxRole.Constant.Builtin.append(word)
            word in keywordRoles -> keywordRoles.getValue(word)
            word in builtinRoles -> builtinRoles.getValue(word)
            nextNonWhitespace(end) == '(' -> SyntaxRole.Function
            word.firstOrNull()?.isUpperCase() == true -> SyntaxRole.Type
            previousNonWhitespace(start) == '.' -> SyntaxRole.Property
            else -> SyntaxRole.Variable
        }
        add(start, end, role)
        return end
    }

    private fun scanOperator(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end] in "+-*/%=!<>|&^~?:") end++
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
