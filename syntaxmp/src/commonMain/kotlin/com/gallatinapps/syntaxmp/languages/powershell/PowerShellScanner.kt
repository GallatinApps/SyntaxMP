package com.gallatinapps.syntaxmp.languages.powershell

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.primitives.findBraceBalancedEnd

internal class PowerShellScanner(
    private val code: String,
    private val language: SyntaxLanguageId,
    private val keywordRoles: LexemeRoleMap = emptyMap(),
    private val constants: Set<String> = emptySet(),
    private val dashOperators: Set<String> = emptySet(),
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()

    fun scan(): List<SyntaxTokenSpan> {
        scanRange(start = 0, limit = code.length)
        return tokens
    }

    private fun scanRange(start: Int, limit: Int) {
        var index = start
        while (index < limit) {
            val char = code[index]
            index = when {
                char.isWhitespace() -> index + 1
                startsWith(index, "<#", limit) -> scanBlockComment(index, limit)
                char == '#' -> scanLineComment(index, limit)
                startsWith(index, "@\"", limit) || startsWith(index, "@'", limit) -> scanHereString(index, limit)
                char == '"' -> scanExpandableString(index, limit)
                char == '\'' -> scanLiteralString(index, limit)
                startsWith(index, "$(", limit) -> scanSubexpression(index, limit)
                char == '$' -> scanVariable(index, limit)
                char == '@' && code.getOrNull(index + 1)?.isPowerShellIdentifierStart() == true -> scanSplat(index, limit)
                char == '`' -> scanBacktick(index, limit)
                char == '-' && code.getOrNull(index + 1)?.isLetter() == true -> scanDashOperator(index, limit)
                char.isDigit() -> scanNumber(index, limit)
                char.isPowerShellIdentifierStart() -> scanWord(index, limit)
                char in "{}[](),.;|" -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                char in "+-*/%=!<>:" -> scanOperator(index, limit)
                else -> index + 1
            }
        }
    }

    private fun scanLineComment(start: Int, limit: Int): Int {
        val end = code.indexOf('\n', start).let { if (it == -1) code.length else it }.coerceAtMost(limit)
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanBlockComment(start: Int, limit: Int): Int {
        val end = code.indexOf("#>", start + 2).let { if (it == -1) code.length else it + 2 }.coerceAtMost(limit)
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanHereString(start: Int, limit: Int): Int {
        val quote = code[start + 1]
        val delimiter = "\n$quote@"
        val close = code.indexOf(delimiter, start + 2).takeIf { it != -1 && it < limit }
        val contentEnd = close ?: limit
        val literalEnd = close?.plus(delimiter.length)?.coerceAtMost(limit) ?: limit
        if (quote == '"') {
            return scanExpandableContent(
                tokenStart = start,
                contentStart = start + 2,
                contentEnd = contentEnd,
                literalEnd = literalEnd,
            )
        }
        add(start, literalEnd, SyntaxRole.String)
        return literalEnd
    }

    private fun scanExpandableString(start: Int, limit: Int): Int {
        val close = findExpandableQuoteEnd(start + 1, limit)
        val literalEnd = if (close < limit) close + 1 else limit
        return scanExpandableContent(
            tokenStart = start,
            contentStart = start + 1,
            contentEnd = close,
            literalEnd = literalEnd,
        )
    }

    private fun findExpandableQuoteEnd(start: Int, limit: Int): Int {
        var index = start
        while (index < limit) {
            when {
                code[index] == '`' -> index += 2
                code[index] == '"' -> return index
                else -> index++
            }
        }
        return limit
    }

    private fun scanExpandableContent(
        tokenStart: Int,
        contentStart: Int,
        contentEnd: Int,
        literalEnd: Int,
    ): Int {
        var index = contentStart
        var segmentStart = tokenStart
        while (index < contentEnd) {
            when {
                code[index] == '`' -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = scanBacktick(index, contentEnd)
                    segmentStart = index
                }
                startsWith(index, "$(", contentEnd) -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = scanSubexpression(index, contentEnd)
                    segmentStart = index
                }
                code[index] == '$' &&
                    (code.getOrNull(index + 1)?.isPowerShellIdentifierStart() == true ||
                        code.getOrNull(index + 1) == '{') -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = scanVariable(index, contentEnd)
                    segmentStart = index
                }
                else -> index++
            }
        }
        if (segmentStart < literalEnd) add(segmentStart, literalEnd, SyntaxRole.String)
        return literalEnd
    }

    private fun scanLiteralString(start: Int, limit: Int): Int {
        var index = start + 1
        while (index < limit) {
            if (code[index] == '\'' && code.getOrNull(index + 1) == '\'') {
                index += 2
            } else if (code[index] == '\'') {
                add(start, index + 1, SyntaxRole.String)
                return index + 1
            } else {
                index++
            }
        }
        add(start, limit, SyntaxRole.String)
        return limit
    }

    private fun scanSubexpression(start: Int, limit: Int): Int {
        val expressionStart = start + 2
        val expressionEnd = findBraceBalancedEnd(
            code = code,
            openIndex = start + 1,
            openBrace = '(',
            closeBrace = ')',
        ).coerceAtMost(limit)
        add(start, expressionStart, SyntaxRole.Escape)
        scanRange(start = expressionStart, limit = expressionEnd)
        if (expressionEnd < limit && code[expressionEnd] == ')') {
            add(expressionEnd, expressionEnd + 1, SyntaxRole.Escape)
            return expressionEnd + 1
        }
        return expressionEnd
    }

    private fun scanVariable(start: Int, limit: Int): Int {
        var end = start + 1
        if (end < limit && code[end] == '{') {
            end++
            while (end < limit && code[end] != '}') end++
            if (end < limit) end++
        } else {
            while (end < limit && (code[end].isPowerShellIdentifierPart() || code[end] == ':')) end++
        }
        val word = code.substring(start, end).lowercase()
        val role = if (word in constants) {
            SyntaxRole.Constant.Builtin.append(word)
        } else {
            SyntaxRole.Variable.Parameter
        }
        add(start, end, role)
        return end
    }

    private fun scanSplat(start: Int, limit: Int): Int {
        var end = start + 1
        while (end < limit && code[end].isPowerShellIdentifierPart()) end++
        add(start, end, SyntaxRole.Variable.append("splatting"))
        return end
    }

    private fun scanBacktick(start: Int, limit: Int): Int {
        val end = (start + 2).coerceAtMost(limit)
        add(start, end, SyntaxRole.Escape)
        return end
    }

    private fun scanDashOperator(start: Int, limit: Int): Int {
        var end = start + 1
        while (end < limit && code[end].isLetter()) end++
        val word = code.substring(start, end).lowercase()
        val role = if (word in dashOperators) SyntaxRole.Operator else SyntaxRole.Variable
        add(start, end, role)
        return end
    }

    private fun scanNumber(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && (code[end].isLetterOrDigit() || code[end] == '.')) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanWord(start: Int, limit: Int): Int {
        var end = start + 1
        while (end < limit && (code[end].isPowerShellIdentifierPart() || code[end] == '-')) end++
        val word = code.substring(start, end)
        val lower = word.lowercase()
        val role = when {
            lower in keywordRoles -> keywordRoles.getValue(lower)
            isCmdletName(word) && isStatementPosition(start) -> SyntaxRole.Function.append("cmdlet")
            nextNonWhitespace(end, limit) == '(' -> SyntaxRole.Function
            else -> SyntaxRole.Variable
        }
        add(start, end, role)
        return end
    }

    private fun scanOperator(start: Int, limit: Int): Int {
        var end = start + 1
        while (end < limit && code[end] in "+-*/%=!<>:") end++
        add(start, end, SyntaxRole.Operator)
        return end
    }

    private fun isCmdletName(word: String): Boolean {
        val dash = word.indexOf('-')
        return dash > 0 &&
            dash < word.lastIndex &&
            word.substring(0, dash).isPascalWord() &&
            word.substring(dash + 1).isPascalWord()
    }

    private fun String.isPascalWord(): Boolean =
        firstOrNull()?.isUpperCase() == true && drop(1).all { it.isLetter() && !it.isUpperCase() }

    private fun isStatementPosition(index: Int): Boolean {
        var cursor = index - 1
        while (cursor >= 0 && code[cursor].isWhitespace() && code[cursor] != '\n') cursor--
        return cursor < 0 || code[cursor] == '\n' || code[cursor] in ";{|("
    }

    private fun nextNonWhitespace(index: Int, limit: Int): Char? {
        var cursor = index
        while (cursor < limit && code[cursor].isWhitespace()) cursor++
        return code.getOrNull(cursor)
    }

    private fun startsWith(index: Int, value: String, limit: Int): Boolean =
        index + value.length <= limit && code.regionMatches(index, value, 0, value.length)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) tokens += SyntaxTokenSpan(start, end, role, language)
    }

}

private fun Char.isPowerShellIdentifierStart(): Boolean =
    this == '_' || isLetter()

private fun Char.isPowerShellIdentifierPart(): Boolean =
    this == '_' || isLetterOrDigit()
