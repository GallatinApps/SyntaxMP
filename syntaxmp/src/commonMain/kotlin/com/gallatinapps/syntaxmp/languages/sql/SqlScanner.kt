package com.gallatinapps.syntaxmp.languages.sql

import com.gallatinapps.syntaxmp.engine.language.LanguageId
import com.gallatinapps.syntaxmp.engine.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal class SqlScanner(
    private val code: String,
    private val language: LanguageId,
    private val keywordRoles: LexemeRoleMap = emptyMap(),
    private val constants: Set<String> = emptySet(),
    private val typeKeywords: Set<String> = emptySet(),
    private val builtinRoles: LexemeRoleMap = emptyMap(),
    private val options: SqlScannerOptions = SqlScannerOptions.Standard,
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        var index = 0
        while (index < code.length) {
            val char = code[index]
            index = when {
                char.isWhitespace() -> index + 1
                startsWith(index, "--") -> scanLineComment(index)
                startsWith(index, "/*") -> scanBlockComment(index)
                options.dollarQuotedStrings && char == '$' -> scanDollarQuotedString(index) ?: (index + 1)
                char.isParameterPrefix() -> scanParameter(index) ?: scanFallback(index)
                options.backtickQuotedIdentifiers && char == '`' -> scanDelimitedIdentifier(
                    start = index,
                    quote = '`',
                    role = SyntaxRole.Property.Quoted,
                )
                options.bracketQuotedIdentifiers && char == '[' -> scanBracketQuotedIdentifier(index)
                isBlobLiteralStart(index) -> scanPrefixedString(index)
                char == '\'' -> scanString(index)
                char == '"' -> scanDelimitedIdentifier(
                    start = index,
                    quote = '"',
                    role = SyntaxRole.Property.Quoted,
                )
                char.isDigit() -> scanNumber(index)
                char.isSqlIdentifierStart() -> scanIdentifier(index)
                char in "(),.;[]" -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                char in "+-*/%=!<>|:&~" -> scanOperator(index)
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

    private fun scanBlockComment(start: Int): Int {
        val end = code.indexOf("*/", start + 2).let { if (it == -1) code.length else it + 2 }
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanString(start: Int): Int {
        var index = start + 1
        while (index < code.length) {
            if (code[index] == '\'' && code.getOrNull(index + 1) == '\'') {
                index += 2
            } else if (code[index] == '\'') {
                add(start, index + 1, SyntaxRole.String)
                return index + 1
            } else {
                index++
            }
        }
        add(start, code.length, SyntaxRole.String)
        return code.length
    }

    private fun scanDelimitedIdentifier(
        start: Int,
        quote: Char,
        role: SyntaxRole,
    ): Int {
        var index = start + 1
        while (index < code.length) {
            if (code[index] == quote && code.getOrNull(index + 1) == quote) {
                index += 2
            } else if (code[index] == quote) {
                add(start, index + 1, role)
                return index + 1
            } else {
                index++
            }
        }
        add(start, code.length, role)
        return code.length
    }

    private fun scanBracketQuotedIdentifier(start: Int): Int {
        var index = start + 1
        while (index < code.length) {
            if (code[index] == ']' && code.getOrNull(index + 1) == ']') {
                index += 2
            } else if (code[index] == ']') {
                add(start, index + 1, SyntaxRole.Property.Quoted)
                return index + 1
            } else {
                index++
            }
        }
        add(start, code.length, SyntaxRole.Property.Quoted)
        return code.length
    }

    private fun scanPrefixedString(start: Int): Int {
        val end = scanStringEnd(start + 1)
        add(start, end, SyntaxRole.String)
        return end
    }

    private fun scanStringEnd(quoteStart: Int): Int {
        var index = quoteStart + 1
        while (index < code.length) {
            if (code[index] == '\'' && code.getOrNull(index + 1) == '\'') {
                index += 2
            } else if (code[index] == '\'') {
                return index + 1
            } else {
                index++
            }
        }
        return code.length
    }

    private fun scanDollarQuotedString(start: Int): Int? {
        val delimiterEnd = dollarQuoteDelimiterEnd(start) ?: return null
        val delimiter = code.substring(start, delimiterEnd)
        val closeStart = code.indexOf(delimiter, startIndex = delimiterEnd)
        if (closeStart == -1) return null
        val end = closeStart + delimiter.length
        add(start, end, SyntaxRole.String)
        return end
    }

    private fun dollarQuoteDelimiterEnd(start: Int): Int? {
        if (code.getOrNull(start) != '$') return null
        val next = code.getOrNull(start + 1) ?: return null
        if (next == '$') return start + 2
        if (!next.isSqlIdentifierStart()) return null
        var end = start + 2
        while (end < code.length && code[end].isDollarQuoteTagPart()) end++
        return if (code.getOrNull(end) == '$') end + 1 else null
    }

    private fun scanNumber(start: Int): Int {
        var end = start
        if (startsWith(start, "0x") || startsWith(start, "0X")) {
            end += 2
            while (end < code.length && (code[end].isDigit() || code[end].lowercaseChar() in 'a'..'f' || code[end] == '_')) {
                end++
            }
            add(start, end, SyntaxRole.Number)
            return end
        }
        while (end < code.length && (code[end].isDigit() || code[end] == '_' || code[end] == '.')) end++
        if (end < code.length && (code[end] == 'e' || code[end] == 'E')) {
            val exponentStart = end
            end++
            if (end < code.length && (code[end] == '-' || code[end] == '+')) end++
            val digitStart = end
            while (end < code.length && (code[end].isDigit() || code[end] == '_')) end++
            if (end == digitStart) end = exponentStart
        }
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanParameter(start: Int): Int? {
        val end = when (code[start]) {
            '?' -> if (options.questionMarkParameters) scanQuestionMarkParameterEnd(start) else null
            '$' -> if ('$' in options.namedParameterPrefixes) scanDollarParameterEnd(start) else null
            else -> if (code[start] in options.namedParameterPrefixes) scanNamedParameterEnd(start) else null
        } ?: return null
        add(start, end, SyntaxRole.Variable.Parameter)
        return end
    }

    private fun scanQuestionMarkParameterEnd(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isDigit()) end++
        return end
    }

    private fun scanNamedParameterEnd(start: Int): Int? {
        var end = start + 1
        if (!code.getOrNull(end).isSqlIdentifierStartOrNull()) return null
        end++
        while (end < code.length && code[end].isSqlIdentifierPart()) end++
        return end
    }

    private fun scanDollarParameterEnd(start: Int): Int? {
        var end = start + 1
        if (!code.getOrNull(end).isSqlIdentifierStartOrNull()) return null
        end++
        while (end < code.length && code[end].isSqlIdentifierPart()) end++
        if (options.dollarParameterQualifiedSuffixes) {
            while (startsWith(end, "::")) {
                val segmentStart = end + 2
                if (!code.getOrNull(segmentStart).isSqlIdentifierStartOrNull()) break
                end = segmentStart + 1
                while (end < code.length && code[end].isSqlIdentifierPart()) end++
            }
            if (code.getOrNull(end) == '(') {
                end++
                while (end < code.length && !code[end].isWhitespace() && code[end] != ')') end++
                if (code.getOrNull(end) == ')') end++
            }
        }
        return end
    }

    private fun scanIdentifier(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isSqlIdentifierPart()) end++
        val word = code.substring(start, end)
        val lower = word.lowercase()
        val role = when {
            lower in constants -> SyntaxRole.Constant.Builtin.append(lower)
            lower in typeKeywords -> SyntaxRole.Type
            lower in keywordRoles -> keywordRoles.getValue(lower)
            lower in builtinRoles -> builtinRoles.getValue(lower)
            nextNonWhitespace(end) == '(' -> SyntaxRole.Function
            previousNonWhitespace(start) == '.' -> SyntaxRole.Property
            else -> SyntaxRole.Variable
        }
        add(start, end, role)
        return end
    }

    private fun scanOperator(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end] in "+-*/%=!<>|:&~") end++
        add(start, end, SyntaxRole.Operator)
        return end
    }

    private fun scanFallback(index: Int): Int =
        if (code[index] in "+-*/%=!<>|:&~") scanOperator(index) else index + 1

    private fun nextNonWhitespace(index: Int): Char? {
        var cursor = index
        while (cursor < code.length && code[cursor].isWhitespace()) cursor++
        return code.getOrNull(cursor)
    }

    private fun previousNonWhitespace(index: Int): Char? {
        var cursor = index - 1
        while (cursor >= 0 && code[cursor].isWhitespace()) cursor--
        return code.getOrNull(cursor)
    }

    private fun startsWith(index: Int, value: String): Boolean =
        code.regionMatches(index, value, 0, value.length)

    private fun isBlobLiteralStart(index: Int): Boolean =
        code[index] in options.blobLiteralPrefixes && code.getOrNull(index + 1) == '\''

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }
}

private fun Char.isParameterPrefix(): Boolean =
    this == '?' || this == ':' || this == '@' || this == '$'

private fun Char.isSqlIdentifierStart(): Boolean =
    this == '_' || isLetter()

private fun Char?.isSqlIdentifierStartOrNull(): Boolean =
    this != null && isSqlIdentifierStart()

private fun Char.isSqlIdentifierPart(): Boolean =
    this == '_' || this == '$' || isLetterOrDigit()

private fun Char.isDollarQuoteTagPart(): Boolean =
    this == '_' || isLetterOrDigit()
