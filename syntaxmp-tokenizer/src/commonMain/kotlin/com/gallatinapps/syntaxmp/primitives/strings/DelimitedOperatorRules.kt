package com.gallatinapps.syntaxmp.primitives.strings

import com.gallatinapps.syntaxmp.role.SyntaxRole

internal data class QuoteOperatorOptions(
    val role: SyntaxRole = SyntaxRole.String,
    val interpolation: List<InterpolationRule> = emptyList(),
    val segmentCount: Int = 1,
)

internal data class PercentLiteralOptions(
    val role: SyntaxRole = SyntaxRole.String,
    val interpolation: List<InterpolationRule> = emptyList(),
)

internal data class QuoteLikeOperatorRule(
    val operators: Map<String, QuoteOperatorOptions>,
) : StringLiteralStartRule {
    private val sortedOperators = operators.keys.sortedByDescending { it.length }
    override val leadingChars: Set<Char> = operators.keys.mapTo(mutableSetOf()) { it.first() }

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (!canStartOperatorLikeLiteral(context, start)) return null
        val operator = sortedOperators.firstOrNull { context.startsWith(start, it) } ?: return null
        val spec = operators.getValue(operator)
        val delimiterStart = start + operator.length
        return if (spec.segmentCount == 1) {
            scanDelimitedOperatorLiteral(context, start, delimiterStart, spec.role, spec.interpolation)
        } else {
            scanMultiSegmentOperator(context, start, delimiterStart, spec)
        }
    }
}

internal data class PercentLiteralRule(
    val marker: Char,
    val specifiers: Map<Char, PercentLiteralOptions>,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf(marker)

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (context.code.getOrNull(start) != marker || !canStartOperatorLikeLiteral(context, start)) return null
        val specifier = context.code.getOrNull(start + 1) ?: return null
        val spec = specifiers[specifier] ?: return null
        return scanDelimitedOperatorLiteral(
            context = context,
            tokenStart = start,
            delimiterStart = start + 2,
            role = spec.role,
            interpolation = spec.interpolation,
        )
    }
}

private fun scanDelimitedOperatorLiteral(
    context: StringLiteralScope,
    tokenStart: Int,
    delimiterStart: Int,
    role: SyntaxRole,
    interpolation: List<InterpolationRule>,
): Int? {
    val end = findDelimitedOperatorEnd(context.code, delimiterStart) ?: return null
    return context.scanStringLiteral(
        StringLiteralRequest(
            tokenStart = tokenStart,
            contentStart = delimiterStart + 1,
            contentEnd = end - 1,
            literalEnd = end,
            closing = NoStringLiteralClosingRule,
            escapes = EscapeMode.Backslash,
            interpolation = interpolation,
            role = role,
        ),
    )
}

private fun scanMultiSegmentOperator(
    context: StringLiteralScope,
    tokenStart: Int,
    delimiterStart: Int,
    spec: QuoteOperatorOptions,
): Int? {
    var end = findDelimitedOperatorEnd(context.code, delimiterStart) ?: return null
    repeat(spec.segmentCount - 1) {
        end = findDelimitedOperatorEnd(context.code, end - 1) ?: return null
    }
    while (end < context.code.length && context.code[end].isLetter()) end++
    context.emit(tokenStart, end, spec.role)
    return end
}

private fun findDelimitedOperatorEnd(code: String, delimiterStart: Int): Int? {
    val delimiter = code.getOrNull(delimiterStart) ?: return null
    if (delimiter.isLetterOrDigit() || delimiter.isWhitespace()) return null
    val close = delimiterClose(delimiter)
    val open = delimiter
    var depth = 0
    var index = delimiterStart + 1
    while (index < code.length) {
        when {
            code[index] == '\\' -> index = (index + 2).coerceAtMost(code.length)
            close != open && code[index] == open -> {
                depth++
                index++
            }
            close != open && code[index] == close && depth > 0 -> {
                depth--
                index++
            }
            code[index] == close -> return index + 1
            else -> index++
        }
    }
    return null
}

private fun delimiterClose(open: Char): Char =
    when (open) {
        '(' -> ')'
        '[' -> ']'
        '{' -> '}'
        '<' -> '>'
        else -> open
    }
