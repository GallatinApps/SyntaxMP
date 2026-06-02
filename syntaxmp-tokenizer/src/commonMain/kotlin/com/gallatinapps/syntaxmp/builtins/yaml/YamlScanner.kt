package com.gallatinapps.syntaxmp.builtins.yaml

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal class YamlScanner(
    private val code: String,
    private val language: LanguageId,
    private val constants: Set<String> = emptySet(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var lineStart = 0
        while (lineStart < code.length) {
            val lineEnd = lineEnd(lineStart)
            val blockScalar = scanLine(lineStart, lineEnd)
            lineStart = if (blockScalar != null) {
                val bodyStart = if (lineEnd < code.length) lineEnd + 1 else lineEnd
                scanBlockScalarBody(bodyStart, blockScalar.parentIndent)
            } else if (lineEnd == code.length) {
                code.length
            } else {
                lineEnd + 1
            }
        }
        return tokens
    }

    private fun scanLine(start: Int, end: Int): BlockScalarStart? {
        val hash = findUnquoted(start, end, '#')
        val contentEnd = if (hash == -1) end else hash
        if (hash != -1) {
            add(hash, end, SyntaxRole.Comment)
        }
        val lineIndent = firstNonWhitespace(start, contentEnd) - start
        val colon = findUnquoted(start, contentEnd, ':')
        if (colon != -1) {
            val keyStart = firstNonWhitespace(start, colon)
            if (keyStart < colon) {
                add(keyStart, colon, SyntaxRole.Property.Name)
            }
            add(colon, colon + 1, SyntaxRole.Punctuation)
        }
        var index = start
        while (index < contentEnd) {
            val char = code[index]
            index = when {
                char.isWhitespace() -> index + 1
                isBlockScalarIndicator(index, contentEnd) -> {
                    scanBlockScalarIndicator(index, contentEnd)
                    return BlockScalarStart(parentIndent = lineIndent)
                }
                char == '"' || char == '\'' -> {
                    val next = findQuotedEnd(index, char, contentEnd)
                    add(index, next, SyntaxRole.String)
                    next
                }
                char == '&' && code.getOrNull(index + 1)?.isYamlAnchorPart() == true ->
                    scanYamlAnchorOrAlias(index, contentEnd, SyntaxRole.Variable.append("yaml").append("anchor"))
                char == '*' && code.getOrNull(index + 1)?.isYamlAnchorPart() == true ->
                    scanYamlAnchorOrAlias(index, contentEnd, SyntaxRole.Variable.append("yaml").append("alias"))
                char == '!' && code.getOrNull(index + 1)?.isWhitespace() != true -> scanYamlTag(index, contentEnd)
                char == '-' && index + 1 < contentEnd && code[index + 1].isWhitespace() -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                char.isDigit() || char == '-' -> scanPlainScalar(index, contentEnd)
                char.isLetter() -> scanPlainScalar(index, contentEnd)
                char in "[]{}," -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                else -> index + 1
            }
        }
        return null
    }

    private fun scanBlockScalarBody(start: Int, parentIndent: Int): Int {
        var lineStart = start
        var bodyEnd = start
        while (lineStart < code.length) {
            val lineEnd = lineEnd(lineStart)
            val first = firstNonWhitespace(lineStart, lineEnd)
            val blank = first >= lineEnd
            val indent = first - lineStart
            if (!blank && indent <= parentIndent) break
            bodyEnd = if (lineEnd < code.length) lineEnd + 1 else lineEnd
            lineStart = if (lineEnd < code.length) lineEnd + 1 else code.length
        }
        if (bodyEnd > start) add(start, bodyEnd, SyntaxRole.String)
        return lineStart
    }

    private fun isBlockScalarIndicator(index: Int, lineEnd: Int): Boolean {
        val char = code[index]
        if (char != '|' && char != '>') return false
        val previous = previousNonWhitespace(index)
        if (previous != ':' && previous != '-') return false
        var cursor = index + 1
        while (cursor < lineEnd && code[cursor] in "+-0123456789") cursor++
        return cursor == lineEnd || code[cursor].isWhitespace()
    }

    private fun scanBlockScalarIndicator(start: Int, lineEnd: Int): Int {
        var end = start + 1
        while (end < lineEnd && code[end] in "+-0123456789") end++
        add(start, end, SyntaxRole.String.append("yaml").append("block-indicator"))
        return end
    }

    private fun scanYamlAnchorOrAlias(start: Int, lineEnd: Int, role: SyntaxRole): Int {
        var end = start + 1
        while (end < lineEnd && code[end].isYamlAnchorPart()) end++
        add(start, end, role)
        return end
    }

    private fun scanYamlTag(start: Int, lineEnd: Int): Int {
        var end = start + 1
        if (code.getOrNull(end) == '!') end++
        if (code.getOrNull(end) == '<') {
            end++
            while (end < lineEnd && code[end] != '>') end++
            if (end < lineEnd) end++
        } else {
            while (end < lineEnd && !code[end].isWhitespace() && code[end] !in "[]{}:,") end++
        }
        add(start, end, SyntaxRole.Type)
        return end
    }

    private fun scanPlainScalar(start: Int, lineEnd: Int): Int {
        var end = start
        while (end < lineEnd && !code[end].isWhitespace() && code[end] !in "[]{}:,") end++
        val word = code.substring(start, end)
        when {
            word in constants ->
                add(start, end, SyntaxRole.Constant.Builtin.append(word))
            word.toDoubleOrNull() != null -> add(start, end, SyntaxRole.Number)
            start > 0 && code[start - 1] == ':' -> add(start, end, SyntaxRole.String)
        }
        return end
    }

    private fun findUnquoted(start: Int, end: Int, target: Char): Int {
        var quote: Char? = null
        var index = start
        while (index < end) {
            val char = code[index]
            if (quote != null) {
                if (char == '\\') index++
                else if (char == quote) quote = null
            } else if (char == '"' || char == '\'') {
                quote = char
            } else if (char == target) {
                return index
            }
            index++
        }
        return -1
    }

    private fun firstNonWhitespace(start: Int, end: Int): Int {
        var index = start
        while (index < end && code[index].isWhitespace()) index++
        return index
    }

    private fun previousNonWhitespace(index: Int): Char? {
        var cursor = index - 1
        while (cursor >= 0 && code[cursor].isWhitespace() && code[cursor] != '\n') cursor--
        return code.getOrNull(cursor)
    }

    private fun lineEnd(start: Int): Int =
        code.indexOf('\n', start).let { if (it == -1) code.length else it }

    private fun findQuotedEnd(start: Int, quote: Char, limit: Int): Int {
        var index = start + 1
        while (index < limit) {
            if (code[index] == '\\') index += 2
            else if (code[index] == quote) return index + 1
            else index++
        }
        return limit
    }

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }

    private data class BlockScalarStart(
        val parentIndent: Int,
    )
}

private fun Char.isYamlAnchorPart(): Boolean =
    isLetterOrDigit() || this == '_' || this == '-'
