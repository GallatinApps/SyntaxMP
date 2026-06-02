package com.gallatinapps.syntaxmp.scanners.script

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralScope
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralRequest
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralScanner
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.primitives.QualifiedNameContextCache
import com.gallatinapps.syntaxmp.primitives.isIdentifierPart
import com.gallatinapps.syntaxmp.primitives.isIdentifierStart
import com.gallatinapps.syntaxmp.primitives.BlockCommentOptions

internal class ScriptLikeScanner(
    override val code: String,
    private val language: LanguageId,
    private val keywordRoles: LexemeRoleMap = emptyMap(),
    private val constants: Set<String> = emptySet(),
    private val typeKeywords: Set<String> = emptySet(),
    private val builtinRoles: LexemeRoleMap = emptyMap(),
    private val options: ScriptLikeScannerOptions = ScriptLikeScannerOptions.Standard,
) : StringLiteralScope {
    private val tokens = mutableListOf<SyntaxTokenSpan>()
    private val stringScanner = StringLiteralScanner(this)
    private val qualifiedNameContext = QualifiedNameContextCache(code, options.qualifiedNames)
    private var previousNonWhitespaceCacheIndex = NoCachedCharacterIndex
    private var previousNonWhitespaceCacheValue = NoCachedCharacter
    private var nextNonWhitespaceCacheIndex = NoCachedCharacterIndex
    private var nextNonWhitespaceCacheValue = NoCachedCharacter

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
                else -> tryScanHashBracketAnnotation(index) ?: tryScanComment(index) ?: tryScanLiteral(index) ?: when {
                    options.literals.regex.enabled && char == '/' && isRegexStart(index) -> scanRegex(index)
                    options.lineStartDecoratorRole != null && char == '@' && isLineStartDecorator(index, limit) ->
                        scanLineStartDecorator(index, limit, options.lineStartDecoratorRole)
                    char in options.identifiers.sigilVariablePrefixes && isSigilVariableStart(index, limit) ->
                        scanSigilVariable(index, limit)
                    char.isDigit() -> scanNumber(index)
                    char.isIdentifierStart() -> scanIdentifier(index, limit)
                    else -> tryScanQualifiedNameMarker(index) ?: when {
                        char in "{}[](),.;:" -> {
                            add(index, index + 1, SyntaxRole.Punctuation)
                            index + 1
                        }
                        char in "+-*/%=!<>|&^~?@" -> scanOperator(index)
                        else -> index + 1
                    }
                }
            }
        }
    }

    private fun tryScanHashBracketAnnotation(start: Int): Int? {
        if (!options.hashBracketAnnotations || !startsWith(start, "#[")) return null
        val close = findBalancedBracketClose(openIndex = start + 1) ?: return null
        add(start, start + 2, SyntaxRole.Annotation)

        val nameStart = firstNonWhitespace(start + 2, close)
        val nameEnd = scanAnnotationNameEnd(nameStart, close)
        if (nameEnd > nameStart) {
            add(nameStart, nameEnd, SyntaxRole.Annotation)
            scanRange(start = nameEnd, limit = close)
        } else {
            scanRange(start = start + 2, limit = close)
        }

        add(close, close + 1, SyntaxRole.Annotation)
        return close + 1
    }

    private fun tryScanComment(start: Int): Int? =
        if (lineCommentPrefixAt(start) != null) {
            scanLineComment(start)
        } else {
            options.blockComments
                ?.takeIf { startsWith(start, it.opener) }
                ?.let { scanBlockComment(start, it) }
        }

    private fun lineCommentPrefixAt(index: Int): String? =
        options.lineComments.prefixesByDescendingLength.firstOrNull { startsWith(index, it) }

    private fun scanLineComment(start: Int): Int {
        val end = code.indexOf('\n', start).let { if (it == -1) code.length else it }
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanBlockComment(start: Int, block: BlockCommentOptions): Int {
        val end = code.indexOf(block.closer, start + block.opener.length).let {
            if (it == -1) code.length else it + block.closer.length
        }
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun tryScanLiteral(start: Int): Int? =
        options.literals.rulesStartingWith(code[start]).firstNotNullOfOrNull { rule ->
            rule.tryMatch(this, start)
        }

    private fun scanRegex(start: Int): Int {
        var index = start + 1
        while (index < code.length) {
            if (code[index] == '\\') index += 2
            else if (code[index] == '/') {
                index++
                while (index < code.length && code[index].isLetter()) index++
                add(start, index, SyntaxRole.String.Regex)
                return index
            } else {
                index++
            }
        }
        return start + 1
    }

    private fun scanLineStartDecorator(start: Int, limit: Int, role: SyntaxRole): Int {
        var end = start + 1
        while (end < limit) {
            if (!code[end].isIdentifierStart()) break
            end++
            while (end < limit && code[end].isIdentifierPart()) end++
            if (code.getOrNull(end) == '.' && code.getOrNull(end + 1)?.isIdentifierStart() == true) {
                end++
            } else {
                break
            }
        }
        add(start, end, role)
        return end
    }

    private fun isLineStartDecorator(start: Int, limit: Int): Boolean {
        if (start + 1 >= limit || !code[start + 1].isIdentifierStart()) return false
        var cursor = start - 1
        while (cursor >= 0 && code[cursor] != '\n') {
            if (!code[cursor].isWhitespace()) return false
            cursor--
        }
        return true
    }

    private fun scanSigilVariable(start: Int, limit: Int): Int {
        var end = start + 1
        if (end < limit && code[end] == '{') {
            end++
            while (end < limit && code[end] != '}') end++
            if (end < limit) end++
        } else {
            while (end < limit && code[end].isIdentifierPart()) end++
        }
        add(start, end, SyntaxRole.Variable.Parameter)
        return end
    }

    private fun isSigilVariableStart(start: Int, limit: Int): Boolean {
        val bodyStart = start + 1
        return bodyStart < limit && (code[bodyStart] == '{' || code[bodyStart].isIdentifierPart())
    }

    private fun scanNumber(start: Int): Int {
        val end = options.numbers.scan(code, start) ?: (start + 1)
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanIdentifier(start: Int, limit: Int = code.length): Int {
        var end = start + 1
        while (end < limit && code[end].isIdentifierPart()) end++
        if (end < limit && code[end] in options.identifiers.suffixes) end++
        val word = code.substring(start, end)
        val qualifiedNameRole = qualifiedNameContext.roleForIdentifier(start, word)
        val role = when {
            qualifiedNameRole != null -> qualifiedNameRole
            word in constants -> SyntaxRole.Constant.Builtin.append(word)
            word in typeKeywords -> SyntaxRole.Type
            word in keywordRoles -> keywordRoles.getValue(word)
            word in builtinRoles -> builtinRoles.getValue(word)
            nextNonWhitespace(end) == '(' -> SyntaxRole.Function
            previousNonWhitespace(start) == '.' -> SyntaxRole.Property
            word.firstOrNull()?.isUpperCase() == true -> SyntaxRole.Type
            else -> SyntaxRole.Variable
        }
        add(start, end, role)
        return end
    }

    private fun tryScanQualifiedNameMarker(start: Int): Int? {
        val marker = qualifiedNameContext.markerAt(start) ?: return null
        add(start, start + marker.length, options.qualifiedNames.role)
        return start + marker.length
    }

    private fun scanOperator(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end] in "+-*/%=!<>|&^~?@") end++
        add(start, end, SyntaxRole.Operator)
        return end
    }

    private fun isRegexStart(index: Int): Boolean {
        val previous = previousNonWhitespace(index)
        return previous == null || previous in "([{=,:;!&|?"
    }

    override fun emit(start: Int, end: Int, role: SyntaxRole) {
        add(start, end, role)
    }

    override fun scanNestedCode(start: Int, endExclusive: Int) {
        scanRange(start = start, limit = endExclusive.coerceAtMost(code.length))
    }

    override fun previousNonWhitespace(index: Int): Char? {
        if (previousNonWhitespaceCacheIndex == index) return previousNonWhitespaceCacheValue.toCachedCharacter()
        var cursor = index - 1
        while (cursor >= 0 && code[cursor].isWhitespace()) cursor--
        val value = code.getOrNull(cursor)
        previousNonWhitespaceCacheIndex = index
        previousNonWhitespaceCacheValue = value?.code ?: NoCachedCharacter
        return value
    }

    override fun nextNonWhitespace(index: Int): Char? {
        if (nextNonWhitespaceCacheIndex == index) return nextNonWhitespaceCacheValue.toCachedCharacter()
        var cursor = index
        while (cursor < code.length && code[cursor].isWhitespace()) cursor++
        val value = code.getOrNull(cursor)
        nextNonWhitespaceCacheIndex = index
        nextNonWhitespaceCacheValue = value?.code ?: NoCachedCharacter
        return value
    }

    private fun Int.toCachedCharacter(): Char? =
        if (this == NoCachedCharacter) {
            null
        } else {
            toChar()
        }

    override fun startsWith(index: Int, value: String): Boolean =
        index + value.length <= code.length && code.regionMatches(index, value, 0, value.length)

    override fun scanNestedIdentifier(start: Int, limit: Int): Int =
        scanIdentifier(start = start, limit = limit.coerceAtMost(code.length))

    override fun scanStringLiteral(request: StringLiteralRequest): Int =
        stringScanner.scan(request)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }

    private fun findBalancedBracketClose(openIndex: Int): Int? {
        var depth = 1
        var index = openIndex + 1
        while (index < code.length) {
            index = when (code[index]) {
                '\'', '"' -> skipQuoted(index)
                '[' -> {
                    depth++
                    index + 1
                }
                ']' -> {
                    depth--
                    if (depth == 0) return index
                    index + 1
                }
                else -> index + 1
            }
        }
        return null
    }

    private fun skipQuoted(start: Int): Int {
        val quote = code[start]
        var index = start + 1
        while (index < code.length) {
            index = if (code[index] == '\\') {
                (index + 2).coerceAtMost(code.length)
            } else if (code[index] == quote) {
                return index + 1
            } else {
                index + 1
            }
        }
        return code.length
    }

    private fun firstNonWhitespace(start: Int, limit: Int): Int {
        var index = start
        while (index < limit && code[index].isWhitespace()) index++
        return index
    }

    private fun scanAnnotationNameEnd(start: Int, limit: Int): Int {
        var index = start
        if (code.getOrNull(index) == '\\') index++
        if (index >= limit || !code[index].isIdentifierStart()) return start
        index++
        while (index < limit && (code[index].isIdentifierPart() || code[index] == '\\')) index++
        return index
    }

    private companion object {
        const val NoCachedCharacterIndex = -1
        const val NoCachedCharacter = -1
    }
}
