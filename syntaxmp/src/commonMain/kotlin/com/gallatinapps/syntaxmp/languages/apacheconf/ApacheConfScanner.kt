package com.gallatinapps.syntaxmp.languages.apacheconf

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal class ApacheConfScanner(
    private val code: String,
    private val language: SyntaxLanguageId,
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var index = 0
        while (index < code.length) {
            val lineEnd = code.indexOf('\n', index).let { if (it == -1) code.length else it }
            scanLine(start = index, end = lineEnd)
            index = (lineEnd + 1).coerceAtMost(code.length)
        }
        return tokens
    }

    private fun scanLine(start: Int, end: Int) {
        var index = firstNonWhitespace(start, end)
        if (index >= end) return
        if (code[index] == '#') {
            add(index, end, SyntaxRole.Comment)
            return
        }
        if (code[index] == '<') {
            scanBlockTag(index, end)
            return
        }

        val directiveStart = index
        while (index < end && code[index].isApacheNamePart()) index++
        if (directiveStart < index) {
            add(directiveStart, index, SyntaxRole.Keyword)
            scanArguments(index, end)
        }
    }

    private fun scanBlockTag(start: Int, end: Int) {
        var index = start
        add(index, index + 1, SyntaxRole.Punctuation)
        index++
        if (index < end && code[index] == '/') {
            add(index, index + 1, SyntaxRole.Punctuation)
            index++
        }
        val tagStart = index
        while (index < end && code[index].isApacheNamePart()) index++
        add(tagStart, index, SyntaxRole.Tag)
        scanArguments(index, end)
        val close = code.lastIndexOf('>', startIndex = end - 1)
        if (close >= start) add(close, close + 1, SyntaxRole.Punctuation)
    }

    private fun scanArguments(start: Int, end: Int) {
        var index = start
        while (index < end) {
            index = when {
                code[index].isWhitespace() -> index + 1
                code[index] == '#' -> {
                    add(index, end, SyntaxRole.Comment)
                    end
                }
                code[index] == '"' || code[index] == '\'' -> scanString(index, end, code[index])
                code[index] == '>' -> index + 1
                code[index] in "=/" -> {
                    add(index, index + 1, SyntaxRole.Operator)
                    index + 1
                }
                else -> scanArgument(index, end)
            }
        }
    }

    private fun scanString(start: Int, limit: Int, quote: Char): Int {
        var index = start + 1
        while (index < limit) {
            if (code[index] == '\\') index += 2
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

    private fun scanArgument(start: Int, end: Int): Int {
        var index = start
        while (index < end && !code[index].isWhitespace() && code[index] !in "<>#") index++
        add(start, index, SyntaxRole.Variable)
        return index
    }

    private fun firstNonWhitespace(start: Int, end: Int): Int {
        var index = start
        while (index < end && code[index].isWhitespace()) index++
        return index
    }

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }
}

private fun Char.isApacheNamePart(): Boolean =
    this == '_' || this == '-' || this == '.' || isLetterOrDigit()
