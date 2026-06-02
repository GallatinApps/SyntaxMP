package com.gallatinapps.syntaxmp.builtins.toml

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal class TomlScanner(
    private val code: String,
    private val language: LanguageId,
    private val constants: Set<String> = emptySet(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var lineStart = 0
        while (lineStart < code.length) {
            val lineEnd = lineEnd(lineStart)
            val scannedEnd = scanLine(lineStart, lineEnd)
            lineStart = when {
                scannedEnd > lineEnd -> scannedEnd
                lineEnd == code.length -> code.length
                else -> lineEnd + 1
            }
        }
        return tokens
    }

    private fun scanLine(start: Int, end: Int): Int {
        val commentStart = findUnquoted(start, end, '#')
        val contentEnd = if (commentStart == -1) end else commentStart
        if (commentStart != -1) {
            add(commentStart, end, SyntaxRole.Comment)
        }

        val first = firstNonWhitespace(start, contentEnd)
        if (first >= contentEnd) return end
        if (code[first] == '[') {
            scanTableHeader(first, contentEnd)
            return end
        }

        val equals = findUnquoted(start, contentEnd, '=')
        return if (equals != -1) {
            scanKey(start, equals)
            add(equals, equals + 1, SyntaxRole.Operator)
            scanValue(equals + 1, contentEnd)
        } else {
            scanValue(start, contentEnd)
        }
    }

    private fun scanTableHeader(start: Int, end: Int) {
        var index = start
        while (index < end && code[index] == '[') {
            add(index, index + 1, SyntaxRole.Punctuation)
            index++
        }
        val tableEnd = findUnquoted(index, end, ']').let { if (it == -1) end else it }
        scanDottedPropertyName(index, tableEnd)
        index = tableEnd
        while (index < end && code[index] == ']') {
            add(index, index + 1, SyntaxRole.Punctuation)
            index++
        }
    }

    private fun scanKey(start: Int, end: Int) {
        scanDottedPropertyName(firstNonWhitespace(start, end), trimTrailingWhitespace(start, end))
    }

    private fun scanDottedPropertyName(start: Int, end: Int) {
        var index = start
        while (index < end) {
            val char = code[index]
            index = when {
                char.isWhitespace() -> index + 1
                char == '"' || char == '\'' -> {
                    val next = findQuotedEnd(index, char, end)
                    add(index, next, SyntaxRole.Property.Name)
                    next
                }
                char == '.' -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                isBareKeyChar(char) -> {
                    val next = readBareKey(index, end)
                    add(index, next, SyntaxRole.Property.Name)
                    next
                }
                else -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
            }
        }
    }

    private fun scanValue(start: Int, end: Int): Int {
        var index = start
        while (index < end) {
            val char = code[index]
            index = when {
                char.isWhitespace() -> index + 1
                startsWith(index, "\"\"\"") || startsWith(index, "'''") -> return scanMultilineString(index)
                char == '"' || char == '\'' -> scanString(index, char, end)
                char.isDigit() || char == '-' || char == '+' -> scanNumberOrDate(index, end)
                isBareKeyChar(char) -> scanInlineKeyOrLiteral(index, end)
                char in "[]{}," -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                char == '=' -> {
                    add(index, index + 1, SyntaxRole.Operator)
                    index + 1
                }
                char == '.' -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                else -> index + 1
            }
        }
        return end
    }

    private fun scanString(start: Int, quote: Char, limit: Int): Int {
        var index = start + 1
        var segmentStart = start
        while (index < limit) {
            when {
                quote == '"' && code[index] == '\\' -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    val escapeEnd = (index + 2).coerceAtMost(limit)
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
        if (segmentStart < limit) add(segmentStart, limit, SyntaxRole.String)
        return limit
    }

    private fun scanMultilineString(start: Int): Int {
        val quote = code.substring(start, start + 3)
        val end = code.indexOf(quote, start + 3).let { if (it == -1) code.length else it + 3 }
        add(start, end, SyntaxRole.String)
        return end
    }

    private fun scanNumberOrDate(start: Int, limit: Int): Int {
        var end = start
        if (code[end] == '-' || code[end] == '+') end++
        while (end < limit && (code[end].isLetterOrDigit() || code[end] in "_:.-+")) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanLiteral(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && (code[end].isLetterOrDigit() || code[end] == '_' || code[end] == '-')) end++
        val word = code.substring(start, end)
        val role = when (word) {
            in constants -> SyntaxRole.Constant.Builtin.append(word)
            else -> SyntaxRole.String
        }
        add(start, end, role)
        return end
    }

    private fun scanInlineKeyOrLiteral(start: Int, limit: Int): Int {
        val keyEnd = readDottedBareKey(start, limit)
        val equals = firstNonWhitespace(keyEnd, limit)
        if (keyEnd > start && equals < limit && code[equals] == '=') {
            scanDottedPropertyName(start, keyEnd)
            add(equals, equals + 1, SyntaxRole.Operator)
            return equals + 1
        }
        return scanLiteral(start, limit)
    }

    private fun findUnquoted(start: Int, end: Int, target: Char): Int {
        var quote: Char? = null
        var index = start
        while (index < end) {
            val char = code[index]
            if (quote != null) {
                if (quote == '"' && char == '\\') {
                    index++
                } else if (char == quote) {
                    quote = null
                }
            } else if (char == '"' || char == '\'') {
                quote = char
            } else if (char == target) {
                return index
            }
            index++
        }
        return -1
    }

    private fun findQuotedEnd(start: Int, quote: Char, limit: Int): Int {
        var index = start + 1
        while (index < limit) {
            if (quote == '"' && code[index] == '\\') index += 2
            else if (code[index] == quote) return index + 1
            else index++
        }
        return limit
    }

    private fun readBareKey(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && isBareKeyChar(code[end])) end++
        return end
    }

    private fun readDottedBareKey(start: Int, limit: Int): Int {
        var end = readBareKey(start, limit)
        while (end < limit && code[end] == '.') {
            val nextPartStart = end + 1
            val nextPartEnd = readBareKey(nextPartStart, limit)
            if (nextPartEnd == nextPartStart) break
            end = nextPartEnd
        }
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

    private fun lineEnd(start: Int): Int =
        code.indexOf('\n', start).let { if (it == -1) code.length else it }

    private fun startsWith(index: Int, value: String): Boolean =
        index + value.length <= code.length && code.regionMatches(index, value, 0, value.length)

    private fun isBareKeyChar(char: Char): Boolean =
        char.isLetterOrDigit() || char == '_' || char == '-'

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }
}
