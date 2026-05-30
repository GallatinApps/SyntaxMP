package com.gallatinapps.syntaxmp.engine.primitives.strings

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.findBraceBalancedEnd
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierPart
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierStart

internal interface InterpolationRule : StringLiteralBoundaryRule

internal object DollarIdentifierInterpolationRule : InterpolationRule {
    override val leadingChars: Set<Char> = setOf('$')

    override fun tryMatch(context: StringLiteralScope, index: Int, contentEnd: Int): Int? {
        if (context.code.getOrNull(index) != '$') return null
        val next = context.code.getOrNull(index + 1) ?: return null
        if (!next.isIdentifierStart()) return null
        context.emit(index, index + 1, SyntaxRole.Escape)
        return context.scanNestedIdentifier(index + 1, contentEnd)
    }
}

internal data class SigilVariableInterpolationRule(
    val prefixes: Set<Char>,
    val role: SyntaxRole = SyntaxRole.Variable.Parameter,
) : InterpolationRule {
    override val leadingChars: Set<Char> = prefixes

    override fun tryMatch(context: StringLiteralScope, index: Int, contentEnd: Int): Int? {
        if (context.code.getOrNull(index) !in prefixes) return null
        var end = index + 1
        if (end < contentEnd && context.code[end] == '{') {
            end++
            while (end < contentEnd && context.code[end] != '}') end++
            if (end < contentEnd) end++
        } else {
            while (end < contentEnd && context.code[end].isIdentifierPart()) end++
        }
        context.emit(index, end, role)
        return end
    }
}

internal data class BalancedInterpolationRule(
    val opener: String,
    val openBrace: Char,
    val closeBrace: Char,
    val ignoreDoubledOpener: Boolean = false,
) : InterpolationRule {
    override val leadingChars: Set<Char>? = opener.firstOrNull()?.let(::setOf)

    override fun tryMatch(context: StringLiteralScope, index: Int, contentEnd: Int): Int? {
        if (!context.startsWith(index, opener)) return null
        if (
            ignoreDoubledOpener &&
            opener.length == 1 &&
            context.startsWith(index + opener.length, opener)
        ) {
            return null
        }
        val expressionStart = index + opener.length
        val openIndex = expressionStart - 1
        val expressionEnd = findBraceBalancedEnd(
            code = context.code,
            openIndex = openIndex,
            openBrace = openBrace,
            closeBrace = closeBrace,
        ).coerceAtMost(contentEnd)
        context.emit(index, expressionStart, SyntaxRole.Escape)
        context.scanNestedCode(expressionStart, expressionEnd)
        if (expressionEnd < contentEnd) {
            context.emit(expressionEnd, expressionEnd + 1, SyntaxRole.Escape)
        }
        return (expressionEnd + 1).coerceAtMost(contentEnd)
    }
}
