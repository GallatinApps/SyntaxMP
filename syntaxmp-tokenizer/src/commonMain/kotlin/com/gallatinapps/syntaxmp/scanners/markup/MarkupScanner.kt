package com.gallatinapps.syntaxmp.scanners.markup

import com.gallatinapps.syntaxmp.language.LanguageId
import com.gallatinapps.syntaxmp.primitives.findBraceBalancedEnd
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.spans.appendEmbeddedSpans
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest

internal class MarkupScanner(
    private val request: TokenizeRequest,
    private val options: MarkupScannerOptions = MarkupScannerOptions(),
) {
    private val code: String = request.code
    private val language: LanguageId = request.languageId
    private val tokens = mutableListOf<SyntaxTokenSpan>()
    private var markupDepth = 0

    fun scan(
        start: Int = 0,
        endExclusive: Int = code.length,
    ): List<SyntaxTokenSpan> {
        tokens.clear()
        markupDepth = 0
        val safeStart = start.coerceIn(0, code.length)
        val safeEnd = endExclusive.coerceIn(safeStart, code.length)
        val contentStart = if (safeStart == 0) {
            options.frontMatterLanguage?.let { scanFrontMatter(languageId = it, endExclusive = safeEnd) } ?: safeStart
        } else {
            safeStart
        }
        scanRange(start = contentStart, endExclusive = safeEnd)
        return tokens.toList()
    }

    private fun scanRange(start: Int, endExclusive: Int) {
        var index = start
        var scriptStart = if (options.startsInScript && markupDepth == 0) start else -1

        fun flushScript(until: Int) {
            if (scriptStart >= 0 && until > scriptStart) {
                tokens.appendEmbeddedSpans(
                    parentCode = request.code,
                    bodyStart = scriptStart,
                    bodyEnd = until,
                    languageLabel = options.expressionLanguage!!.value,
                    tokenizeEmbedded = request::tokenizeEmbedded,
                )
            }
            scriptStart = -1
        }

        while (index < endExclusive) {
            index = when {
                startsWith(index, "<!--", endExclusive) -> {
                    flushScript(index)
                    scanComment(index, endExclusive)
                }
                startsWith(index, "<![CDATA[", endExclusive) -> {
                    flushScript(index)
                    scanCdata(index, endExclusive)
                }
                startsWith(index, "<?", endExclusive) -> {
                    flushScript(index)
                    scanProcessingInstruction(index, endExclusive)
                }
                startsWith(index, "<!", endExclusive) -> {
                    flushScript(index)
                    scanDeclaration(index, endExclusive)
                }
                shouldScanTag(index, endExclusive) -> {
                    flushScript(index)
                    scanTag(index, endExclusive).also { next ->
                        scriptStart = if (options.startsInScript && markupDepth == 0) next else -1
                    }
                }
                shouldScanExpression(index, endExclusive) -> {
                    flushScript(index)
                    scanExpression(index, ExpressionContext.MarkupText, endExclusive) ?: (index + 1)
                }
                shouldScanEntity(index) -> {
                    flushScript(index)
                    scanEntity(index, endExclusive) ?: (index + 1)
                }
                else -> {
                    if (options.startsInScript && markupDepth == 0 && scriptStart < 0) {
                        scriptStart = index
                    }
                    index + 1
                }
            }
        }

        flushScript(endExclusive)
    }

    private fun scanFrontMatter(
        languageId: LanguageId,
        endExclusive: Int,
    ): Int {
        if (!startsWith(0, "---", endExclusive)) return 0
        val openerEnd = lineEnd(0, endExclusive)
        if (openerEnd != 3) return 0
        val closingStart = findLineStartingWith("---", openerEnd + 1, endExclusive) ?: return 0
        val closingEnd = lineEnd(closingStart, endExclusive)
        add(0, openerEnd, SyntaxRole.Markup.Frontmatter)
        tokens.appendEmbeddedSpans(
            parentCode = request.code,
            bodyStart = (openerEnd + 1).coerceAtMost(endExclusive),
            bodyEnd = closingStart,
            languageLabel = languageId.value,
            tokenizeEmbedded = request::tokenizeEmbedded,
        )
        add(closingStart, closingEnd, SyntaxRole.Markup.Frontmatter)
        return (closingEnd + 1).coerceAtMost(endExclusive)
    }

    private fun scanComment(start: Int, endExclusive: Int): Int {
        val closing = code.indexOf("-->", start + 4)
        val end = if (closing == -1 || closing + 3 > endExclusive) endExclusive else closing + 3
        add(start, end, SyntaxRole.Comment)
        return end
    }

    private fun scanCdata(start: Int, endExclusive: Int): Int {
        val contentStart = (start + "<![CDATA[".length).coerceAtMost(endExclusive)
        val closingStart = code.indexOf("]]>", contentStart)
        add(start, contentStart, SyntaxRole.Markup.Cdata)
        return if (closingStart == -1 || closingStart + 3 > endExclusive) {
            add(contentStart, endExclusive, SyntaxRole.String)
            endExclusive
        } else {
            add(contentStart, closingStart, SyntaxRole.String)
            add(closingStart, closingStart + 3, SyntaxRole.Markup.Cdata)
            closingStart + 3
        }
    }

    private fun scanProcessingInstruction(start: Int, endExclusive: Int): Int {
        val closing = code.indexOf("?>", start + 2)
        val end = if (closing == -1 || closing + 2 > endExclusive) endExclusive else closing + 2
        add(start, end, SyntaxRole.Annotation)
        return end
    }

    private fun scanDeclaration(start: Int, endExclusive: Int): Int {
        val end = findTagEnd(start + 2, endExclusive)
        add(start, end, SyntaxRole.Annotation)
        return end
    }

    private fun scanTag(start: Int, endExclusive: Int): Int {
        var index = start
        val closing = startsWith(start, "</", endExclusive)
        val fragment = startsWith(start, "<>", endExclusive) || startsWith(start, "</>", endExclusive)
        if (closing) {
            add(start, (start + 2).coerceAtMost(endExclusive), SyntaxRole.Punctuation)
            index = (start + 2).coerceAtMost(endExclusive)
            if (markupDepth > 0) markupDepth--
        } else {
            add(start, (start + 1).coerceAtMost(endExclusive), SyntaxRole.Punctuation)
            index = (start + 1).coerceAtMost(endExclusive)
        }

        index = skipWhitespace(index, endExclusive)
        val tagStart = index
        while (index < endExclusive && code[index].isMarkupNamePart()) index++
        val sourceTagName = code.substring(tagStart, index)
        if (sourceTagName.isNotBlank()) {
            add(tagStart, index, SyntaxRole.Tag)
        }
        val tagName = if (options.preserveTagNameCase) sourceTagName else sourceTagName.lowercase()

        if (options.typeArgumentTags && sourceTagName.isNotBlank() && code.getOrNull(index) == '<') {
            index = scanTypeArguments(index, endExclusive)
        }

        val attributes = mutableMapOf<String, String?>()
        var selfClosing = fragment
        while (index < endExclusive) {
            index = skipWhitespace(index, endExclusive)
            when {
                index >= endExclusive -> return index
                startsWith(index, "/>", endExclusive) -> {
                    add(index, index + 2, SyntaxRole.Punctuation)
                    selfClosing = true
                    index += 2
                    break
                }
                code[index] == '>' -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index++
                    break
                }
                code[index] == '=' -> {
                    add(index, index + 1, SyntaxRole.Operator)
                    index++
                }
                else -> {
                    index = scanExpression(index, ExpressionContext.Tag, endExclusive)
                        ?: scanAttribute(index, attributes, endExclusive)
                }
            }
        }

        if (!closing && tagName in options.rawTextTags) {
            val bodyStart = index
            val closingStart = findClosingTagStart(tagName, bodyStart, endExclusive)
            val embeddedLanguageLabel = options.rawTextLanguageForTag(
                tagName,
                attributes["lang"],
            )
            if (embeddedLanguageLabel != null) {
                tokens.appendEmbeddedSpans(
                    parentCode = request.code,
                    bodyStart = bodyStart,
                    bodyEnd = closingStart ?: endExclusive,
                    languageLabel = embeddedLanguageLabel,
                    tokenizeEmbedded = request::tokenizeEmbedded,
                )
            }
            return closingStart ?: endExclusive
        }

        if (!closing && !selfClosing && sourceTagName.isNotBlank()) {
            markupDepth++
        }
        return index
    }

    private fun scanAttribute(
        start: Int,
        attributes: MutableMap<String, String?>,
        endExclusive: Int,
    ): Int {
        var index = start
        while (
            index < endExclusive &&
            !code[index].isWhitespace() &&
            code[index] !in "=/{>}<"
        ) {
            index++
        }
        if (index <= start) return (start + 1).coerceAtMost(endExclusive)
        val name = code.substring(start, index)
        add(start, index, attributeRole(name))

        index = skipWhitespace(index, endExclusive)
        var value: String? = null
        if (index < endExclusive && code[index] == '=') {
            add(index, index + 1, SyntaxRole.Operator)
            index = skipWhitespace(index + 1, endExclusive)
            val result = scanAttributeValue(index, endExclusive)
            value = result.value
            index = result.endExclusive
        }
        attributes[name.lowercase()] = value
        return index
    }

    private fun scanAttributeValue(start: Int, endExclusive: Int): AttributeValueScan {
        if (start >= endExclusive) return AttributeValueScan(value = null, endExclusive = start)
        val quote = code[start]
        if (quote == '"' || quote == '\'') {
            var index = start + 1
            var segmentStart = start
            var hasEntity = false
            while (index < endExclusive && code[index] != quote) {
                val entityEnd = scanEntity(index, endExclusive)
                if (entityEnd != null) {
                    hasEntity = true
                    if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                    index = entityEnd
                    segmentStart = index
                } else {
                    index++
                }
            }
            val hasClosingQuote = index < endExclusive && code[index] == quote
            val end = if (hasClosingQuote) index + 1 else endExclusive
            if (hasEntity) {
                if (segmentStart < end) add(segmentStart, end, SyntaxRole.String)
            } else {
                add(start, end, SyntaxRole.String)
            }
            val valueStart = (start + 1).coerceAtMost(end)
            val valueEnd = if (hasClosingQuote) index else end
            return AttributeValueScan(
                value = code.substring(valueStart, valueEnd.coerceAtLeast(valueStart)),
                endExclusive = end,
            )
        }
        if (quote == '{') {
            val end = scanExpression(start, ExpressionContext.Tag, endExclusive)
                ?: (start + 1).coerceAtMost(endExclusive)
            return AttributeValueScan(value = null, endExclusive = end)
        }

        var index = start
        var segmentStart = start
        var hasEntity = false
        while (
            index < endExclusive &&
            !code[index].isWhitespace() &&
            code[index] != '>' &&
            !startsWith(index, "/>", endExclusive)
        ) {
            val entityEnd = scanEntity(index, endExclusive)
            if (entityEnd != null) {
                hasEntity = true
                if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
                index = entityEnd
                segmentStart = index
            } else {
                index++
            }
        }
        if (hasEntity) {
            if (segmentStart < index) add(segmentStart, index, SyntaxRole.String)
        } else {
            add(start, index, SyntaxRole.String)
        }
        return AttributeValueScan(value = code.substring(start, index), endExclusive = index)
    }

    private fun scanExpression(
        start: Int,
        context: ExpressionContext,
        endExclusive: Int,
    ): Int? {
        val rule = expressionRuleAt(start, context, endExclusive) ?: return null
        val contentStart = (start + rule.opener.length).coerceAtMost(endExclusive)
        val close = expressionClose(start, rule, endExclusive)
        var expressionStart = contentStart
        val markerEnd = rule.marker.markerEnd(code = code, contentStart = contentStart, close = close)
        if (markerEnd != null) {
            add(start, markerEnd, markerRole(rule.marker))
            expressionStart = markerEnd
        } else {
            add(start, contentStart, rule.delimiterRole)
        }

        tokens.appendEmbeddedSpans(
            parentCode = request.code,
            bodyStart = expressionStart,
            bodyEnd = close,
            languageLabel = options.expressionLanguage!!.value,
            tokenizeEmbedded = request::tokenizeEmbedded,
        )
        if (close < endExclusive) {
            val end = (close + rule.closer.length).coerceAtMost(endExclusive)
            add(close, end, rule.delimiterRole)
            return end
        }
        return endExclusive
    }

    private fun expressionRuleAt(
        start: Int,
        context: ExpressionContext,
        endExclusive: Int,
    ): MarkupExpressionRule? =
        options.orderedExpressionRules.firstOrNull { rule ->
            startsWith(start, rule.opener, endExclusive) &&
                when (context) {
                    ExpressionContext.Tag -> rule.allowedInsideTag
                    ExpressionContext.MarkupText -> {
                        if (markupDepth > 0) rule.allowedInsideMarkup else rule.allowedAtTopLevel
                    }
                }
        }

    private fun expressionClose(
        start: Int,
        rule: MarkupExpressionRule,
        endExclusive: Int,
    ): Int =
        if (rule.opener == "{" && rule.closer == "}") {
            findBraceBalancedEnd(code = code, openIndex = start).coerceAtMost(endExclusive)
        } else {
            code.indexOf(rule.closer, start + rule.opener.length)
                .let { if (it == -1 || it + rule.closer.length > endExclusive) endExclusive else it }
        }

    private fun markerRole(marker: MarkupExpressionMarker): SyntaxRole =
        when (marker) {
            is MarkupExpressionMarker.FirstCharKeyword -> marker.role
            MarkupExpressionMarker.None -> SyntaxRole.Markup.Expression
        }

    private fun scanTypeArguments(start: Int, endExclusive: Int): Int {
        var index = start
        var depth = 0
        while (index < endExclusive) {
            val char = code[index]
            index = when {
                char.isWhitespace() -> index + 1
                char == '<' -> {
                    depth++
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                char == '>' -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    depth--
                    index++
                    if (depth == 0) return index
                    index
                }
                char.isLetter() || char == '_' -> scanTypeArgumentIdentifier(index, endExclusive)
                char in "[],.?" -> {
                    add(index, index + 1, SyntaxRole.Punctuation)
                    index + 1
                }
                char in ":|&=" -> {
                    add(index, index + 1, SyntaxRole.Operator)
                    index + 1
                }
                else -> index + 1
            }
        }
        return start + 1
    }

    private fun scanTypeArgumentIdentifier(start: Int, endExclusive: Int): Int {
        var index = start + 1
        while (index < endExclusive && (code[index].isLetterOrDigit() || code[index] == '_')) index++
        val word = code.substring(start, index)
        val role = if (word.firstOrNull()?.isUpperCase() == true) SyntaxRole.Type else SyntaxRole.Variable
        add(start, index, role)
        return index
    }

    private fun attributeRole(name: String): SyntaxRole =
        options.directiveAttributes.roleFor(name) ?: SyntaxRole.Attribute

    private fun findTagEnd(start: Int, endExclusive: Int): Int {
        var index = start
        var quote: Char? = null
        while (index < endExclusive) {
            val char = code[index]
            if (quote != null) {
                if (char == quote) quote = null
            } else if (char == '"' || char == '\'') {
                quote = char
            } else if (char == '>') {
                return index + 1
            }
            index++
        }
        return endExclusive
    }

    private fun findClosingTagStart(
        tagName: String,
        start: Int,
        endExclusive: Int,
    ): Int? {
        var cursor = start
        while (cursor < endExclusive) {
            val next = code.indexOf("</", cursor)
            if (next == -1 || next >= endExclusive) return null
            var nameStart = (next + 2).coerceAtMost(endExclusive)
            nameStart = skipWhitespace(nameStart, endExclusive)
            var nameEnd = nameStart
            while (nameEnd < endExclusive && code[nameEnd].isMarkupNamePart()) nameEnd++
            val closingName = code.substring(nameStart, nameEnd)
                .let { if (options.preserveTagNameCase) it else it.lowercase() }
            if (closingName == tagName) {
                return next
            }
            cursor = next + 2
        }
        return null
    }

    private fun findLineStartingWith(
        value: String,
        start: Int,
        endExclusive: Int,
    ): Int? {
        var lineStart = start
        while (lineStart < endExclusive) {
            val lineEnd = lineEnd(lineStart, endExclusive)
            if (code.substring(lineStart, lineEnd).trim() == value) {
                return lineStart
            }
            lineStart = lineEnd + 1
        }
        return null
    }

    private fun lineEnd(start: Int, endExclusive: Int): Int {
        val end = code.indexOf('\n', start)
        return if (end == -1 || end > endExclusive) endExclusive else end
    }

    private fun shouldScanTag(index: Int, endExclusive: Int): Boolean =
        if (options.startsInScript || options.expressionRules.isNotEmpty()) {
            isLikelyTagStart(index, endExclusive)
        } else {
            code.getOrNull(index) == '<'
        }

    private fun isLikelyTagStart(index: Int, endExclusive: Int): Boolean {
        if (code.getOrNull(index) != '<') return false
        if (index + 1 >= endExclusive) return false
        val next = code[index + 1]
        return next.isLetter() ||
            next == '>' ||
            (next == '/' && index + 2 < endExclusive && (code[index + 2].isLetter() || code[index + 2] == '>'))
    }

    private fun shouldScanExpression(index: Int, endExclusive: Int): Boolean =
        expressionRuleAt(index, ExpressionContext.MarkupText, endExclusive) != null

    private fun shouldScanEntity(index: Int): Boolean =
        code[index] == '&' && (!options.startsInScript || markupDepth > 0)

    private fun scanEntity(start: Int, endExclusive: Int): Int? {
        val end = findMarkupEntityEnd(code = code, start = start) ?: return null
        if (end > endExclusive) return null
        add(start, end, SyntaxRole.Escape)
        return end
    }

    private fun skipWhitespace(start: Int, endExclusive: Int): Int {
        var index = start
        while (index < endExclusive && code[index].isWhitespace()) index++
        return index
    }

    private fun startsWith(
        index: Int,
        value: String,
        endExclusive: Int,
    ): Boolean =
        index >= 0 &&
            index + value.length <= endExclusive &&
            code.regionMatches(index, value, 0, value.length)

    private fun add(start: Int, end: Int, role: SyntaxRole) {
        if (end > start) {
            tokens += SyntaxTokenSpan(start, end, role, language)
        }
    }
}

private data class AttributeValueScan(
    val value: String?,
    val endExclusive: Int,
)

private enum class ExpressionContext {
    MarkupText,
    Tag,
}

private fun Char.isMarkupNamePart(): Boolean =
    isLetterOrDigit() || this == '_' || this == '-' || this == ':' || this == '.'
