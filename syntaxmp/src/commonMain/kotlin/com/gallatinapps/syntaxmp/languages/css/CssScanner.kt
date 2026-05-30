package com.gallatinapps.syntaxmp.languages.css

import com.gallatinapps.syntaxmp.engine.language.SyntaxLanguageId
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.primitives.findBraceBalancedEnd

internal class CssScanner(
    private val code: String,
    private val language: SyntaxLanguageId,
    private val atRules: Set<String> = emptySet(),
    private val namedConstants: Set<String> = emptySet(),
    private val preprocessorKeywordRoles: Map<String, SyntaxRole> = emptyMap(),
    private val options: CssScannerOptions = CssScannerOptions.Standard,
) {
    private val tokens = mutableListOf<SyntaxTokenSpan>()
    private var valueMode = false

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
                startsWith(index, "/*") -> scanBlockComment(index)
                char == '"' || char == '\'' -> scanString(index, char)
                else -> tryScanInterpolation(index, limit) ?: tryScanVariable(index) ?: when {
                    char == '@' -> scanAtRule(index)
                    char == '#' -> scanHash(index)
                    startsWith(index, "--") -> scanCustomIdentifier(index)
                    char == '.' && code.getOrNull(index + 1)?.isCssIdentifierStart() == true -> scanSelector(index)
                    char == ':' -> scanPseudoOrPunctuation(index)
                    char == '!' && startsWith(index, "!important") -> scanImportant(index)
                    char.isDigit() || (char == '.' && code.getOrNull(index + 1)?.isDigit() == true) -> {
                        scanNumber(index)
                    }
                    char.isCssIdentifierStart() -> scanIdentifier(index)
                    char in "{}[](),;>" -> {
                        if (char == ';' || char == '{' || char == '}') valueMode = false
                        add(index, index + 1, SyntaxRole.Punctuation)
                        index + 1
                    }
                    char == '&' && options.parentSelector is ParentSelectorOptions.Enabled -> scanParentSelector(index)
                    char in "+-*/%=~|^$&" -> scanOperator(index)
                    else -> index + 1
                }
            }
        }
    }

    private fun scanBlockComment(start: Int): Int {
        val end = code.indexOf("*/", start + 2).let { if (it == -1) code.length else it + 2 }
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanString(start: Int, quote: Char): Int {
        var index = start + 1
        var segmentStart = start
        while (index < code.length) {
            val interpolationOpener = interpolationOpenerAt(index)
            when {
                interpolationOpener != null -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = scanInterpolation(index, code.length, interpolationOpener)
                    segmentStart = index
                }
                code[index] == '\\' -> {
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    val escapeEnd = (index + 2).coerceAtMost(code.length)
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
        if (segmentStart < code.length) add(segmentStart, code.length, SyntaxRole.String)
        return code.length
    }

    private fun tryScanInterpolation(start: Int, limit: Int): Int? {
        val opener = interpolationOpenerAt(start) ?: return null
        return scanInterpolation(start, limit, opener)
    }

    private fun interpolationOpenerAt(index: Int): String? =
        options.interpolation.opener?.takeIf { it.isNotEmpty() && startsWith(index, it) }

    private fun scanInterpolation(start: Int, limit: Int, opener: String): Int {
        val expressionStart = start + opener.length
        val expressionEnd = findBraceBalancedEnd(
            code = code,
            openIndex = expressionStart - 1,
        ).coerceAtMost(limit)
        add(start, expressionStart, SyntaxRole.Escape)
        val previousValueMode = valueMode
        scanRange(start = expressionStart, limit = expressionEnd)
        valueMode = previousValueMode
        if (expressionEnd < limit && code[expressionEnd] == '}') {
            add(expressionEnd, expressionEnd + 1, SyntaxRole.Escape)
            return expressionEnd + 1
        }
        return expressionEnd
    }

    private fun scanAtRule(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isCssIdentifierPart()) end++
        val name = code.substring(start + 1, end).lowercase()
        val role = preprocessorKeywordRoles[name] ?: SyntaxRole.Keyword.AtRule
        add(start, end, role)
        return end
    }

    private fun tryScanVariable(start: Int): Int? {
        if (options.variableRules.none { it.prefix == code[start] }) return null
        val nameStart = start + 1
        if (code.getOrNull(nameStart)?.isCssIdentifierStart() != true) return null
        var end = nameStart + 1
        while (end < code.length && code[end].isCssIdentifierPart()) end++
        val name = code.substring(nameStart, end).lowercase()
        val rule = options.variableRules.firstOrNull { rule ->
            rule.prefix == code[start] && (!rule.onlyWhenUnknownAtRule || name !in atRules)
        } ?: return null
        add(start, end, rule.role)
        return end
    }

    private fun scanHash(start: Int): Int {
        val colorEnd = scanColorEnd(start)
        if (valueMode && colorEnd != null) {
            add(start, colorEnd, SyntaxRole.Constant.Color)
            return colorEnd
        }
        return scanSelector(start)
    }

    private fun scanColorEnd(start: Int): Int? {
        var end = start + 1
        while (end < code.length && code[end].isHexDigit()) end++
        val length = end - start - 1
        return if (length == 3 || length == 4 || length == 6 || length == 8) end else null
    }

    private fun scanCustomIdentifier(start: Int): Int {
        var end = start + 2
        while (end < code.length && code[end].isCssIdentifierPart()) end++
        val role = if (nextNonWhitespace(end) == ':') {
            valueMode = true
            SyntaxRole.Property
        } else {
            SyntaxRole.Variable.append("css").append("custom-property")
        }
        add(start, end, role)
        return end
    }

    private fun scanSelector(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isCssIdentifierPart()) end++
        add(start, end, SyntaxRole.Attribute)
        return end
    }

    private fun scanPseudoOrPunctuation(start: Int): Int {
        val prefixLength = if (code.getOrNull(start + 1) == ':') 2 else 1
        val nameStart = start + prefixLength
        if (nameStart < code.length && code[nameStart].isCssIdentifierStart()) {
            var end = nameStart + 1
            while (end < code.length && code[end].isCssIdentifierPart()) end++
            add(start, nameStart, SyntaxRole.Punctuation)
            add(nameStart, end, SyntaxRole.Attribute.Pseudo)
            return end
        }
        add(start, start + 1, SyntaxRole.Punctuation)
        return start + 1
    }

    private fun scanImportant(start: Int): Int {
        add(start, start + "!important".length, SyntaxRole.Constant.Builtin.append("important"))
        return start + "!important".length
    }

    private fun scanParentSelector(start: Int): Int {
        val parentSelector = options.parentSelector as? ParentSelectorOptions.Enabled ?: return start + 1
        add(start, start + 1, parentSelector.role)
        return start + 1
    }

    private fun scanNumber(start: Int): Int {
        var end = start
        if (code[end] == '.') end++
        while (end < code.length && code[end].isDigit()) end++
        if (end < code.length && code[end] == '.') {
            end++
            while (end < code.length && code[end].isDigit()) end++
        }
        while (end < code.length && (code[end].isLetter() || code[end] == '%')) end++
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanIdentifier(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isCssIdentifierPart()) end++
        val word = code.substring(start, end)
        val lower = word.lowercase()
        val role = when {
            previousNonWhitespace(start) == '[' || nextNonWhitespace(end) == '=' -> {
                SyntaxRole.Attribute
            }
            nextNonWhitespace(end) == ':' && !isPseudoClass(start) -> {
                valueMode = true
                SyntaxRole.Property
            }
            nextNonWhitespace(end) == '(' -> {
                SyntaxRole.Function
            }
            valueMode && lower in namedConstants -> {
                SyntaxRole.Constant.Builtin.append(lower)
            }
            valueMode -> SyntaxRole.Variable
            else -> SyntaxRole.Tag
        }
        add(start, end, role)
        return end
    }

    private fun scanOperator(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end] in "+-*/%=~|^$&") end++
        add(start, end, SyntaxRole.Operator)
        return end
    }

    private fun isPseudoClass(start: Int): Boolean =
        previousNonWhitespace(start) == ':'

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
        code.regionMatches(index, value, 0, value.length)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }

}

private fun Char.isCssIdentifierStart(): Boolean =
    this == '_' || this == '-' || isLetter()

private fun Char.isCssIdentifierPart(): Boolean =
    isCssIdentifierStart() || isDigit()

private fun Char.isHexDigit(): Boolean =
    isDigit() || this in 'a'..'f' || this in 'A'..'F'
