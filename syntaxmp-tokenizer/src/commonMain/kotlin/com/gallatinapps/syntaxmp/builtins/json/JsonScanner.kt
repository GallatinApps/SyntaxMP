package com.gallatinapps.syntaxmp.builtins.json

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal class JsonScanner(
    private val code: String,
    private val language: LanguageId,
    private val constants: Set<String> = emptySet(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var index = 0
        while (index < code.length) {
            val char = code[index]
            index = when {
                char.isWhitespace() -> index + 1
                char == '"' -> scanString(index)
                char == '-' || char.isDigit() -> scanNumber(index)
                char.isLetter() -> scanKeyword(index)
                char in "{}[],:." -> {
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

    private fun scanString(start: Int): Int {
        val end = findStringEnd(start, '"')
        val nonWhitespace = nextNonWhitespace(end)
        val role = if (nonWhitespace < code.length && code[nonWhitespace] == ':') {
            SyntaxRole.Property.Name
        } else {
            SyntaxRole.String
        }
        addStringWithEscapes(start, end, role)
        return end
    }

    private fun scanNumber(start: Int): Int {
        var index = start
        if (code[index] == '-') index++
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
        add(start, index, SyntaxRole.Number)
        return index
    }

    private fun scanKeyword(start: Int): Int {
        var end = start
        while (end < code.length && code[end].isLetter()) end++
        val word = code.substring(start, end)
        if (word in constants) {
            add(start, end, SyntaxRole.Constant.Builtin.append(word))
        }
        return end
    }

    private fun findStringEnd(start: Int, quote: Char): Int {
        var index = start + 1
        while (index < code.length) {
            if (code[index] == '\\') {
                index += 2
            } else if (code[index] == quote) {
                return index + 1
            } else {
                index++
            }
        }
        return code.length
    }

    private fun addStringWithEscapes(
        start: Int,
        end: Int,
        role: SyntaxRole,
    ) {
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

    private fun nextNonWhitespace(start: Int): Int {
        var index = start
        while (index < code.length && code[index].isWhitespace()) index++
        return index
    }

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }
}
