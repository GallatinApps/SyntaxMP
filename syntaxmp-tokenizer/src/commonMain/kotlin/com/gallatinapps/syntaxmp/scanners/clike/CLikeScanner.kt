package com.gallatinapps.syntaxmp.scanners.clike

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralScope
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralRequest
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralScanner
import com.gallatinapps.syntaxmp.primitives.BlockCommentOptions
import com.gallatinapps.syntaxmp.primitives.LexemeRoleMap
import com.gallatinapps.syntaxmp.primitives.QualifiedNameContextCache
import com.gallatinapps.syntaxmp.primitives.isIdentifierPart
import com.gallatinapps.syntaxmp.primitives.isIdentifierStart

internal class CLikeScanner(
    override val code: String,
    private val language: LanguageId,
    private val keywordRoles: LexemeRoleMap = emptyMap(),
    private val constants: Set<String> = emptySet(),
    private val typeKeywords: Set<String> = emptySet(),
    private val builtinRoles: LexemeRoleMap = emptyMap(),
    private val options: CLikeScannerOptions = CLikeScannerOptions.Standard,
) : StringLiteralScope {
    private val keywords: Set<String> = keywordRoles.keys
    private val tokens = mutableListOf<SyntaxTokenSpan>()
    private val stringScanner = StringLiteralScanner(this)
    private val qualifiedNameContext = QualifiedNameContextCache(code, options.qualifiedNames)
    private val braceContexts = mutableListOf<BraceContext>()
    private var previousNonWhitespaceCacheIndex = NoCachedCharacterIndex
    private var previousNonWhitespaceCacheValue = NoCachedCharacter
    private var nextNonWhitespaceCacheIndex = NoCachedCharacterIndex
    private var nextNonWhitespaceCacheValue = NoCachedCharacter
    private var parenDepth = 0
    private var enumPending = false
    private var structPending = false
    private var interfacePending = false
    private var typeAliasPending = false
    private var constantDeclarationPending = false
    private var functionDeclarationPending = false
    private var typeDeclarationPending = false

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
                else -> tryScanComment(index)
                    ?: tryScanBacktickIdentifier(index)
                    ?: tryScanLiteral(index)
                    ?: tryScanBracketAnnotation(index)
                    ?: when {
                    options.literals.regex.enabled && char == '/' && isRegexStart(index) -> scanRegex(index)
                    char == options.preprocessor.linePrefix -> scanPreprocessor(index)
                    isAnnotationPrefix(char, index) -> scanAnnotation(index)
                    char.isDigit() -> tryScanNumber(index) ?: run {
                        add(index, index + 1, SyntaxRole.Number)
                        index + 1
                    }
                    char == '.' && code.getOrNull(index + 1)?.isDigit() == true ->
                        tryScanNumber(index) ?: run {
                            add(index, index + 1, SyntaxRole.Punctuation)
                            index + 1
                        }
                    char in options.identifiers.sigilVariablePrefixes && isSigilVariableStart(index, limit) ->
                        scanSigilVariable(index, limit)
                    char.isIdentifierStart() -> scanIdentifier(index, limit)
                    else -> tryScanQualifiedNameMarker(index) ?: when {
                        char in "{}[](),.;" -> scanPunctuation(index)
                        char in "+-*/%=!<>|&^~?:" -> scanOperator(index)
                        else -> index + 1
                    }
                }
            }
        }
    }

    private fun tryScanComment(start: Int): Int? =
        if (lineCommentPrefixAt(start) != null) {
            scanLineComment(start)
        } else {
            blockCommentAt(start)?.let { scanBlockComment(start, it) }
        }

    private fun lineCommentPrefixAt(index: Int): String? =
        options.comments.line.prefixesByDescendingLength.firstOrNull { startsWith(index, it) }

    private fun blockCommentAt(index: Int): BlockCommentOptions? =
        options.comments.block?.takeIf { startsWith(index, it.opener) }

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

    private fun scanAnnotation(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end].isIdentifierPart()) end++
        add(start, end, SyntaxRole.Annotation)
        return end
    }

    private fun tryScanBracketAnnotation(start: Int): Int? {
        if (options.annotations.bracketRequiresLineStart && !isAtLineStart(start)) return null
        val prefix = options.annotations.bracketPrefixesByDescendingLength.firstOrNull { startsWith(start, it) }
            ?: return null
        if (prefix == "[[") {
            val closeIndex = findClosingMarker(start + prefix.length, "]]") ?: return null
            add(start, closeIndex + 2, SyntaxRole.Annotation)
            return closeIndex + 2
        }
        val open = prefix.last()
        val close = when (open) {
            '[' -> ']'
            else -> return null
        }
        val closeIndex = findBalancedClose(openIndex = start + prefix.length - 1, open = open, close = close)
            ?: return null
        add(start, closeIndex + 1, SyntaxRole.Annotation)
        return closeIndex + 1
    }

    private fun isAnnotationPrefix(char: Char, index: Int): Boolean {
        val matchesPrefix = char == options.annotations.prefix || char in options.annotations.additionalPrefixes
        if (!matchesPrefix) return false
        return !options.annotations.ignorePrefixAfterIdentifier ||
            code.getOrNull(index - 1)?.isIdentifierPart() != true
    }

    private fun scanPreprocessor(start: Int): Int {
        val end = code.indexOf('\n', start).let { if (it == -1) code.length else it }
        add(start, end, SyntaxRole.Annotation)
        return end
    }

    private fun tryScanNumber(start: Int): Int? {
        val end = options.numbers.scan(code, start) ?: return null
        if (end <= start) return null
        add(start, end, SyntaxRole.Number)
        return end
    }

    private fun scanIdentifier(start: Int, limit: Int = code.length): Int {
        var end = start + 1
        while (end < limit && code[end].isIdentifierPart()) end++
        val word = code.substring(start, end)
        val identifiers = options.identifiers
        val qualifiedNameRole = qualifiedNameContext.roleForIdentifier(start, word)
        val constantDeclarationRole = consumeConstantDeclarationRole(word, end, identifiers)
        val functionDeclarationRole = consumeFunctionDeclarationRole(word, end, identifiers)
        val typeDeclarationRole = consumeTypeDeclarationRole(word)
        val builtinRole = builtinRoleForIdentifier(word, end)
        val role = when {
            qualifiedNameRole != null -> qualifiedNameRole
            word == "_" && identifiers.wildcardIdentifierRole != null -> identifiers.wildcardIdentifierRole
            identifiers.enumMemberRole != null && insideEnumContext() && isEnumMemberCandidate(end, identifiers) ->
                identifiers.enumMemberRole
            constantDeclarationRole != null -> constantDeclarationRole
            functionDeclarationRole != null -> functionDeclarationRole
            typeDeclarationRole != null -> typeDeclarationRole
            word in constants -> SyntaxRole.Constant.Builtin.append(word)
            word in typeKeywords -> SyntaxRole.Type
            word in keywordRoles -> keywordRoles.getValue(word)
            builtinRole != null -> builtinRole
            identifiers.labelDeclarationRole != null && code.getOrNull(end) == '@' ->
                identifiers.labelDeclarationRole
            identifiers.structFieldRole != null && insideStructContext() && isStructFieldCandidate(start, end) ->
                identifiers.structFieldRole
            identifiers.propertyBeforeAssignment && nextNonWhitespace(end) == '=' ->
                SyntaxRole.Property
            identifiers.propertyBeforeColon && isPropertyBeforeColon(end) ->
                SyntaxRole.Property
            identifiers.namedArgumentRole != null &&
                parenDepth > 0 &&
                previousNonWhitespace(start) != ':' &&
                nextNonWhitespace(end) == '=' ->
                identifiers.namedArgumentRole
            identifiers.propertyBeforeColonMode == PropertyBeforeColonMode.ObjectLikeOnly &&
                insideObjectLikeContext() &&
                parenDepth == 0 &&
                isPropertyBeforeColon(end) ->
                SyntaxRole.Property
            identifiers.typeAfterColon && previousNonWhitespace(start) == ':' &&
                word.firstOrNull()?.isUpperCase() == true -> SyntaxRole.Type
            insideStructContext() && previousNonWhitespace(start) == '.' && word.firstOrNull()?.isUpperCase() == true ->
                SyntaxRole.Type
            identifiers.classifyUppercaseCallsAsTypes && word.firstOrNull()?.isUpperCase() == true -> SyntaxRole.Type
            nextNonWhitespace(end) == '(' -> functionCallRole(word, start, identifiers)
            identifiers.macroSuffix && nextNonWhitespace(end) == '!' -> SyntaxRole.Function.Macro
            previousNonWhitespace(start) == '.' -> SyntaxRole.Property
            word.firstOrNull()?.isUpperCase() == true -> SyntaxRole.Type
            else -> SyntaxRole.Variable
        }
        updateIdentifierContext(word, role)
        add(start, end, role)
        return end
    }

    private fun scanSigilVariable(start: Int, limit: Int): Int {
        var end = start + 1
        while (end < limit && code[end].isIdentifierPart()) end++
        add(start, end, options.identifiers.sigilVariableRole)
        return end
    }

    private fun isSigilVariableStart(start: Int, limit: Int): Boolean {
        val bodyStart = start + 1
        return bodyStart < limit && code[bodyStart].isIdentifierPart()
    }

    private fun updateIdentifierContext(word: String, role: SyntaxRole) {
        val isKeyword = role.isKeyword()
        when {
            isKeyword && word == "enum" && options.identifiers.enumMemberRole != null -> {
                enumPending = true
            }
            isKeyword && word == "struct" && options.identifiers.structFieldRole != null -> {
                structPending = true
            }
            isKeyword &&
                word == "interface" &&
                options.identifiers.propertyBeforeColonMode == PropertyBeforeColonMode.ObjectLikeOnly -> {
                interfacePending = true
            }
            isKeyword &&
                word == "type" &&
                options.identifiers.propertyBeforeColonMode == PropertyBeforeColonMode.ObjectLikeOnly -> {
                typeAliasPending = true
            }
            isKeyword && word in options.identifiers.constantDeclarationKeywords -> {
                constantDeclarationPending = true
            }
            isKeyword && word in options.identifiers.functionDeclarationKeywords -> {
                functionDeclarationPending = true
            }
            isKeyword && word in options.identifiers.typeDeclarationKeywords -> {
                typeDeclarationPending = true
            }
        }
    }

    private fun tryScanBacktickIdentifier(start: Int): Int? {
        val identifiers = options.identifiers
        val defaultRole = identifiers.backtickIdentifierRole ?: return null
        if (code.getOrNull(start) != '`') return null
        val closeIndex = code.indexOf('`', startIndex = start + 1).takeIf { it != -1 } ?: return null
        val end = closeIndex + 1
        val role = consumeFunctionDeclarationRole(
            word = code.substring(start + 1, closeIndex),
            identifierEnd = end,
            identifiers = identifiers,
        ) ?: if (nextNonWhitespace(end) == '(') {
            if (previousNonWhitespace(start) == '.') {
                identifiers.memberFunctionRole ?: SyntaxRole.Function
            } else {
                SyntaxRole.Function
            }
        } else {
            defaultRole
        }
        add(start, end, role)
        return end
    }

    private fun functionCallRole(
        word: String,
        identifierStart: Int,
        identifiers: IdentifierOptions,
    ): SyntaxRole =
        when {
            previousNonWhitespace(identifierStart) == '.' ->
                identifiers.memberFunctionRole ?: SyntaxRole.Function
            word in builtinRoles -> builtinRoles.getValue(word)
            else -> SyntaxRole.Function
        }

    private fun builtinRoleForIdentifier(word: String, identifierEnd: Int): SyntaxRole? {
        val role = builtinRoles[word] ?: return null
        return if (role.isFunction() && nextNonWhitespace(identifierEnd) != '(') null else role
    }

    private fun consumeFunctionDeclarationRole(
        word: String,
        identifierEnd: Int,
        identifiers: IdentifierOptions,
    ): SyntaxRole? {
        if (!functionDeclarationPending || word in keywords || word in typeKeywords) return null
        if (word.firstOrNull()?.isUpperCase() == true && nextNonWhitespace(identifierEnd) != '(') return null
        if (nextNonWhitespace(identifierEnd) != '(') return null
        functionDeclarationPending = false
        return SyntaxRole.Function.Declaration
    }

    private fun consumeTypeDeclarationRole(word: String): SyntaxRole? {
        if (!typeDeclarationPending || word in keywords || word in typeKeywords) return null
        typeDeclarationPending = false
        return SyntaxRole.Type
    }

    private fun consumeConstantDeclarationRole(
        word: String,
        identifierEnd: Int,
        identifiers: IdentifierOptions,
    ): SyntaxRole? {
        if (!constantDeclarationPending || word in keywords || word in typeKeywords) return null
        if (!identifiers.constantDeclarationRequiresAssignment) {
            constantDeclarationPending = false
            return SyntaxRole.Constant
        }
        return when (constantDeclarationDecision(identifierEnd)) {
            ConstantDeclarationDecision.Match -> {
                constantDeclarationPending = false
                SyntaxRole.Constant
            }
            ConstantDeclarationDecision.Skip -> null
            ConstantDeclarationDecision.Reject -> {
                constantDeclarationPending = false
                null
            }
        }
    }

    private fun constantDeclarationDecision(identifierEnd: Int): ConstantDeclarationDecision {
        var cursor = identifierEnd
        while (cursor < code.length && code[cursor].isWhitespace()) cursor++
        if (code.getOrNull(cursor) == '(' || code.getOrNull(cursor) == '.') {
            return ConstantDeclarationDecision.Reject
        }
        while (cursor < code.length) {
            val char = code[cursor]
            when {
                char.isWhitespace() -> cursor++
                char == '=' -> return ConstantDeclarationDecision.Match
                char in ";,){" -> return ConstantDeclarationDecision.Reject
                char.isIdentifierStart() -> return ConstantDeclarationDecision.Skip
                else -> cursor++
            }
        }
        return ConstantDeclarationDecision.Reject
    }

    private fun tryScanQualifiedNameMarker(start: Int): Int? {
        val marker = qualifiedNameContext.markerAt(start) ?: return null
        add(start, start + marker.length, options.qualifiedNames.role)
        return start + marker.length
    }

    private fun scanOperator(start: Int): Int {
        var end = start + 1
        while (end < code.length && code[end] in "+-*/%=!<>|&^~?:") end++
        if (code[start] == '=') {
            functionDeclarationPending = false
            typeDeclarationPending = false
        }
        add(start, end, SyntaxRole.Operator)
        return end
    }

    private fun scanPunctuation(start: Int): Int {
        when (code[start]) {
            '{' -> {
                braceContexts += when {
                    enumPending -> BraceContext.Enum
                    structPending -> BraceContext.Struct
                    shouldEnterObjectLikeContext(start) -> BraceContext.ObjectLike
                    else -> BraceContext.Other
                }
                enumPending = false
                structPending = false
                interfacePending = false
                typeDeclarationPending = false
                if (braceContexts.lastOrNull() == BraceContext.ObjectLike) {
                    typeAliasPending = false
                }
                functionDeclarationPending = false
            }
            '}' -> {
                if (braceContexts.isNotEmpty()) braceContexts.removeAt(braceContexts.lastIndex)
            }
            '(' -> parenDepth++
            ')' -> if (parenDepth > 0) parenDepth--
            ';' -> {
                enumPending = false
                structPending = false
                interfacePending = false
                typeAliasPending = false
                constantDeclarationPending = false
                functionDeclarationPending = false
                typeDeclarationPending = false
            }
        }
        add(start, start + 1, SyntaxRole.Punctuation)
        return start + 1
    }

    private fun insideEnumContext(): Boolean =
        braceContexts.lastOrNull() == BraceContext.Enum

    private fun insideStructContext(): Boolean =
        braceContexts.lastOrNull() == BraceContext.Struct

    private fun insideObjectLikeContext(): Boolean =
        braceContexts.lastOrNull() == BraceContext.ObjectLike

    private fun shouldEnterObjectLikeContext(openBraceIndex: Int): Boolean {
        if (options.identifiers.propertyBeforeColonMode != PropertyBeforeColonMode.ObjectLikeOnly) {
            return false
        }
        val previous = previousNonWhitespace(openBraceIndex)
        val previousWord = previousIdentifierBefore(openBraceIndex)
        return interfacePending ||
            (typeAliasPending && previous == '=') ||
            previous == '=' ||
            (insideObjectLikeContext() && previous == ':') ||
            previousWord == "return"
    }

    private fun isEnumMemberCandidate(identifierEnd: Int, identifiers: IdentifierOptions): Boolean {
        val next = nextNonWhitespace(identifierEnd)
        return next == null ||
            next in "=,;}" ||
            (identifiers.enumMemberAllowsPayload && next in "({")
    }

    private fun isStructFieldCandidate(identifierStart: Int, identifierEnd: Int): Boolean {
        val next = nextNonWhitespace(identifierEnd)
        val previous = previousNonWhitespace(identifierStart)
        val previousWord = previousIdentifierBefore(identifierStart)
        val fieldLikePosition = isLineStart(identifierStart) || previous == '{' || previous == '*' || previous == ','
        val typeKeywordPosition = previousWord in typeKeywords
        return (fieldLikePosition || typeKeywordPosition) && (next == null || next !in "({")
    }

    private fun isPropertyBeforeColon(identifierEnd: Int): Boolean {
        var cursor = identifierEnd
        while (cursor < code.length && code[cursor].isWhitespace()) cursor++
        if (code.getOrNull(cursor) == ':') return true
        if (code.getOrNull(cursor) != '?') return false
        cursor++
        while (cursor < code.length && code[cursor].isWhitespace()) cursor++
        return code.getOrNull(cursor) == ':'
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

    private fun findBalancedClose(openIndex: Int, open: Char, close: Char): Int? {
        var depth = 1
        var index = openIndex + 1
        while (index < code.length) {
            index = when (code[index]) {
                '\'', '"' -> skipQuoted(index)
                open -> {
                    depth++
                    index + 1
                }
                close -> {
                    depth--
                    if (depth == 0) return index
                    index + 1
                }
                else -> index + 1
            }
        }
        return null
    }

    private fun findClosingMarker(start: Int, marker: String): Int? {
        var index = start
        while (index < code.length) {
            index = if (code[index] == '\'' || code[index] == '"') {
                skipQuoted(index)
            } else if (startsWith(index, marker)) {
                return index
            } else {
                index + 1
            }
        }
        return null
    }

    private fun isAtLineStart(start: Int): Boolean {
        return isLineStart(start)
    }

    private fun isLineStart(start: Int): Boolean {
        var cursor = start - 1
        while (cursor >= 0 && code[cursor] != '\n') {
            if (!code[cursor].isWhitespace()) return false
            cursor--
        }
        return true
    }

    private fun previousIdentifierBefore(index: Int): String? {
        var end = index - 1
        while (end >= 0 && code[end].isWhitespace()) end--
        if (end < 0 || !code[end].isIdentifierPart()) return null
        var start = end
        while (start >= 0 && code[start].isIdentifierPart()) start--
        return code.substring(start + 1, end + 1)
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

    private enum class BraceContext {
        Enum,
        Struct,
        ObjectLike,
        Other,
    }

    private enum class ConstantDeclarationDecision {
        Match,
        Skip,
        Reject,
    }

    private companion object {
        const val NoCachedCharacterIndex = -1
        const val NoCachedCharacter = -1
    }
}

private fun SyntaxRole.isKeyword(): Boolean =
    this == SyntaxRole.Keyword || value.startsWith("${SyntaxRole.Keyword.value}.")

private fun SyntaxRole.isFunction(): Boolean =
    this == SyntaxRole.Function || value.startsWith("${SyntaxRole.Function.value}.")
