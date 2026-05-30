package com.gallatinapps.syntaxmp.engine.primitives.strings

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierPart

internal class StringLiteralScanner(
    private val context: StringLiteralScope,
) {
    fun scan(request: StringLiteralRequest): Int {
        val hardEnd = request.contentEnd?.coerceIn(0, context.code.length) ?: context.code.length
        val finalEnd = request.literalEnd?.coerceIn(0, context.code.length) ?: hardEnd
        val stopChars = StringLiteralStopChars.from(request)
        var index = request.contentStart.coerceIn(0, hardEnd)
        var segmentStart = request.tokenStart.coerceIn(0, context.code.length)

        fun flushSegment(until: Int) {
            val safeEnd = until.coerceIn(segmentStart, context.code.length)
            if (segmentStart < safeEnd) {
                context.emit(segmentStart, safeEnd, request.role)
            }
        }

        while (index < hardEnd) {
            val stopIndex = stopChars?.nextStop(context.code, index, hardEnd)
            if (stopIndex != null && stopIndex > index) {
                index = stopIndex
                continue
            }

            val interpolationMatch = tryMatchBoundaryRule(request.interpolation, index, hardEnd)
            if (interpolationMatch != null) {
                flushSegment(index)
                interpolationMatch.replay(context)
                index = interpolationMatch.resumeIndex.coerceIn(index, hardEnd)
                segmentStart = index
                continue
            }

            val formatMatch = tryMatchBoundaryRule(request.formats, index, hardEnd)
            if (formatMatch != null) {
                flushSegment(index)
                formatMatch.replay(context)
                index = formatMatch.resumeIndex.coerceIn(index, hardEnd)
                segmentStart = index
                continue
            }

            val close = request.closing.tryMatch(context.code, index, hardEnd)
            when {
                request.escapes == EscapeMode.Backslash && context.code[index] == '\\' -> {
                    flushSegment(index)
                    val escapeEnd = (index + 2).coerceAtMost(hardEnd)
                    context.emit(index, escapeEnd, SyntaxRole.Escape)
                    index = escapeEnd
                    segmentStart = index
                }
                request.doubledQuoteEscapes && context.startsWith(index, "\"\"") -> {
                    index += 2
                }
                close != null -> {
                    flushSegment(close.literalEnd)
                    return close.literalEnd
                }
                else -> index++
            }
        }

        flushSegment(finalEnd)
        return finalEnd
    }

    private fun tryMatchBoundaryRule(
        rules: List<StringLiteralBoundaryRule>,
        index: Int,
        hardEnd: Int,
    ): BoundaryRuleResult? {
        val char = context.code[index]
        rules.forEach { rule ->
            val leadingChars = rule.leadingChars
            if (leadingChars != null && char !in leadingChars) return@forEach
            val recordingContext = RecordingStringLiteralScope(context)
            val resume = rule.tryMatch(recordingContext, index, hardEnd)
            if (resume != null) {
                return BoundaryRuleResult(resumeIndex = resume, actions = recordingContext.actions)
            }
        }
        return null
    }
}

private data class StringLiteralStopChars(
    val chars: Set<Char>,
) {
    fun nextStop(code: String, start: Int, hardEnd: Int): Int {
        var index = start
        while (index < hardEnd) {
            if (code[index] in chars) return index
            index++
        }
        return hardEnd
    }

    companion object {
        fun from(request: StringLiteralRequest): StringLiteralStopChars? {
            val chars = mutableSetOf<Char>()
            chars += request.closing.leadingChars ?: return null
            if (!chars.addBoundaryLeadingChars(request.interpolation)) return null
            if (!chars.addBoundaryLeadingChars(request.formats)) return null
            if (request.escapes == EscapeMode.Backslash) chars += '\\'
            if (request.doubledQuoteEscapes) chars += '"'
            return StringLiteralStopChars(chars)
        }

        private fun MutableSet<Char>.addBoundaryLeadingChars(rules: List<StringLiteralBoundaryRule>): Boolean {
            rules.forEach { rule ->
                val leadingChars = rule.leadingChars ?: return false
                addAll(leadingChars)
            }
            return true
        }
    }
}

private data class BoundaryRuleResult(
    val resumeIndex: Int,
    val actions: List<RecordedAction>,
) {
    fun replay(context: StringLiteralScope) {
        actions.forEach { it.replay(context) }
    }
}

private sealed interface RecordedAction {
    fun replay(context: StringLiteralScope)

    data class Emit(
        val start: Int,
        val end: Int,
        val role: SyntaxRole,
    ) : RecordedAction {
        override fun replay(context: StringLiteralScope) {
            context.emit(start, end, role)
        }
    }

    data class ScanNestedCode(
        val start: Int,
        val endExclusive: Int,
    ) : RecordedAction {
        override fun replay(context: StringLiteralScope) {
            context.scanNestedCode(start, endExclusive)
        }
    }

    data class ScanNestedIdentifier(
        val start: Int,
        val limit: Int,
    ) : RecordedAction {
        override fun replay(context: StringLiteralScope) {
            context.scanNestedIdentifier(start, limit)
        }
    }

    data class ScanStringLiteral(
        val request: StringLiteralRequest,
    ) : RecordedAction {
        override fun replay(context: StringLiteralScope) {
            context.scanStringLiteral(request)
        }
    }
}

private class RecordingStringLiteralScope(
    private val delegate: StringLiteralScope,
) : StringLiteralScope {
    val actions = mutableListOf<RecordedAction>()

    override val code: String
        get() = delegate.code

    override fun emit(start: Int, end: Int, role: SyntaxRole) {
        actions += RecordedAction.Emit(start, end, role)
    }

    override fun scanNestedCode(start: Int, endExclusive: Int) {
        actions += RecordedAction.ScanNestedCode(start, endExclusive)
    }

    override fun previousNonWhitespace(index: Int): Char? =
        delegate.previousNonWhitespace(index)

    override fun nextNonWhitespace(index: Int): Char? =
        delegate.nextNonWhitespace(index)

    override fun startsWith(index: Int, value: String): Boolean =
        delegate.startsWith(index, value)

    override fun scanNestedIdentifier(start: Int, limit: Int): Int {
        var end = start
        while (end < limit && code[end].isIdentifierPart()) end++
        actions += RecordedAction.ScanNestedIdentifier(start, limit)
        return end
    }

    override fun scanStringLiteral(request: StringLiteralRequest): Int {
        actions += RecordedAction.ScanStringLiteral(request)
        return request.literalEnd ?: request.contentEnd ?: code.length
    }
}
