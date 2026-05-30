package com.gallatinapps.syntaxmp.languages.json5

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal class Json5Scanner(
    private val code: String,
    private val language: SyntaxLanguageId,
    private val constants: Set<String> = emptySet(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var index = 0
        while (index < code.length) {
            index = when {
                code[index].isWhitespace() -> index + 1
                startsWith(index, "//") -> scanLineComment(index)
                startsWith(index, "/*") -> scanBlockComment(index)
                code[index] == '"' || code[index] == '\'' -> scanString(index, code[index])
                code[index] == '-' || code[index] == '+' -> scanSignedConstant(index) ?: scanNumber(index)
                code[index].isDigit() -> scanNumber(index)
                code[index].isJson5IdentifierStart() -> scanIdentifier(index)
                code[index] in "{}[],:." -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                else -> {
                    add(index, index + 1, SyntaxRole.Operator)
                    index + 1
                }
            }
        }
        return tokens
    }

    private fun scanSignedConstant(start: Int): Int? {
        val valueStart = start + 1
        val constant = constants
            .filter { it == "Infinity" || it == "NaN" }
            .firstOrNull {
                startsWith(valueStart, it) &&
                    !code.getOrNull(valueStart + it.length).isJson5IdentifierPartOrFalse()
            }
            ?: return null
        val end = valueStart + constant.length
        add(start, end, SyntaxRole.Constant.Builtin.append(constant))
        return end
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

    private fun scanString(start: Int, quote: Char): Int {
        val end = findStringEnd(start, quote)
        val role = if (nextNonWhitespace(end) == ':') {
            SyntaxRole.Property.Name
        } else {
            SyntaxRole.String
        }
        addStringWithEscapes(start, end, role)
        return end
    }

    private fun scanNumber(start: Int): Int {
        var index = start
        if (code[index] == '-' || code[index] == '+') index++
        if (index + 1 < code.length && code[index] == '0' && (code[index + 1] == 'x' || code[index + 1] == 'X')) {
            index += 2
            while (index < code.length && code[index].isHexDigit()) index++
        } else {
            while (index < code.length && code[index].isDigit()) index++
            if (index < code.length && code[index] == '.') {
                index++
                while (index < code.length && code[index].isDigit()) index++
            }
            if (index < code.length && (code[index] == 'e' || code[index] == 'E')) {
                index++
                if (index < code.length && (code[index] == '+' || code[index] == '-')) index++
                while (index < code.length && code[index].isDigit()) index++
            }
        }
        add(start, index, SyntaxRole.Number)
        return index
    }

    private fun scanIdentifier(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isJson5IdentifierPart()) end++
        val word = code.substring(start, end)
        val role = when (word) {
            in constants ->
                SyntaxRole.Constant.Builtin.append(word)
            else -> if (nextNonWhitespace(end) == ':') {
                SyntaxRole.Property.Name
            } else {
                SyntaxRole.Variable
            }
        }
        add(start, end, role)
        return end
    }

    private fun findStringEnd(start: Int, quote: Char): Int {
        var index = start + 1
        while (index < code.length) {
            if (code[index] == '\\') index += 2
            else if (code[index] == quote) return index + 1
            else index++
        }
        return code.length
    }

    private fun addStringWithEscapes(start: Int, end: Int, role: SyntaxRole) {
        var segmentStart = start
        var index = start + 1
        while (index < end - 1) {
            if (code[index] == '\\') {
                if (segmentStart < index) add(segmentStart, index, role)
                val escapeEnd = (index + 2).coerceAtMost(end)
                add(index, escapeEnd, SyntaxRole.Escape)
                index = escapeEnd
                segmentStart = index
            } else {
                index++
            }
        }
        if (segmentStart < end) add(segmentStart, end, role)
    }

    private fun nextNonWhitespace(start: Int): Char? {
        var index = start
        while (index < code.length && code[index].isWhitespace()) index++
        return code.getOrNull(index)
    }

    private fun startsWith(index: Int, value: String): Boolean =
        index + value.length <= code.length && code.regionMatches(index, value, 0, value.length)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }
}

private fun Char.isJson5IdentifierStart(): Boolean =
    this == '_' || this == '$' || isLetter()

private fun Char.isJson5IdentifierPart(): Boolean =
    isJson5IdentifierStart() || isDigit()

private fun Char?.isJson5IdentifierPartOrFalse(): Boolean =
    this?.isJson5IdentifierPart() == true

private fun Char.isHexDigit(): Boolean =
    isDigit() || this in 'a'..'f' || this in 'A'..'F'
