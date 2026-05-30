package com.gallatinapps.syntaxmp.engine.primitives.strings

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierPart

internal data class HashWrappedStringRule(
    val quote: Char,
    val allowTripleQuote: Boolean,
    val interpolationFactory: (hashCount: Int) -> List<InterpolationRule> = { emptyList() },
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf('#')

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (context.code.getOrNull(start) != '#') return null
        var hashEnd = start
        while (context.code.getOrNull(hashEnd) == '#') hashEnd++
        if (context.code.getOrNull(hashEnd) != quote) return null
        val delimiterLength = if (allowTripleQuote && context.startsWith(hashEnd, quote.toString().repeat(3))) 3 else 1
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = hashEnd + delimiterLength,
                closing = FixedStringLiteralClosingRule(quote.toString().repeat(delimiterLength) + "#".repeat(hashEnd - start)),
                escapes = EscapeMode.None,
                interpolation = interpolationFactory(hashEnd - start),
                role = role,
            ),
        )
    }
}

internal data class HashCountPrefixedStringRule(
    val prefixes: Set<String>,
    val quote: Char,
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    private val sortedPrefixes = prefixes.sortedByDescending { it.length }
    override val leadingChars: Set<Char> = prefixes.mapNotNullTo(mutableSetOf()) { it.firstOrNull() }

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (start > 0 && context.code[start - 1].isIdentifierPart()) return null
        val prefix = sortedPrefixes.firstOrNull { context.startsWith(start, it) } ?: return null
        var quoteStart = start + prefix.length
        while (context.code.getOrNull(quoteStart) == '#') quoteStart++
        if (context.code.getOrNull(quoteStart) != quote) return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = quoteStart + 1,
                closing = FixedStringLiteralClosingRule(quote.toString() + "#".repeat(quoteStart - start - prefix.length)),
                escapes = EscapeMode.None,
                role = role,
            ),
        )
    }
}

internal data class ParenthesizedRawStringRule(
    val prefixes: Set<String>,
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    private val sortedPrefixes = prefixes.sortedByDescending { it.length }
    override val leadingChars: Set<Char> = prefixes.mapNotNullTo(mutableSetOf()) { it.firstOrNull() }

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (start > 0 && context.code[start - 1].isIdentifierPart()) return null
        val prefix = sortedPrefixes.firstOrNull { context.startsWith(start, it) } ?: return null
        val quoteStart = start + prefix.length
        if (!context.startsWith(quoteStart, "\"")) return null
        var parenStart = quoteStart + 1
        while (parenStart < context.code.length && context.code[parenStart] != '(' && context.code[parenStart] != '\n') {
            parenStart++
        }
        if (parenStart >= context.code.length || context.code[parenStart] != '(') return null
        val delimiter = context.code.substring(quoteStart + 1, parenStart)
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = parenStart + 1,
                closing = FixedStringLiteralClosingRule(")$delimiter\""),
                escapes = EscapeMode.None,
                role = role,
            ),
        )
    }
}

internal data class AtSignVerbatimStringRule(
    val interpolationPrefixes: Set<String> = emptySet(),
    override val leadingChars: Set<Char> = setOf('@', '$'),
    val interpolation: List<InterpolationRule> = emptyList(),
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    private val sortedInterpolationPrefixes = interpolationPrefixes.sortedByDescending { it.length }

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        val interpolationPrefix = sortedInterpolationPrefixes.firstOrNull { prefix ->
            context.startsWith(start, prefix) && context.code.getOrNull(start + prefix.length) == '"'
        }
        val quoteStart = when {
            interpolationPrefix != null -> start + interpolationPrefix.length
            context.startsWith(start, "@\"") -> start + 1
            else -> return null
        }
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = quoteStart + 1,
                closing = FixedStringLiteralClosingRule("\""),
                escapes = EscapeMode.None,
                interpolation = if (interpolationPrefix == null) emptyList() else interpolation,
                doubledQuoteEscapes = true,
                role = role,
            ),
        )
    }
}

internal data class RepeatedQuoteRawStringRule(
    val quote: Char,
    val minimumQuoteCount: Int,
    val interpolationPrefix: PrefixRun? = null,
    override val leadingChars: Set<Char> = buildSet {
        add(quote)
        interpolationPrefix?.let { add(it.char) }
    },
    val interpolation: List<InterpolationRule> = emptyList(),
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        var quoteStart = start
        var interpolated = false
        if (interpolationPrefix != null && context.code.getOrNull(start) == interpolationPrefix.char) {
            while (context.code.getOrNull(quoteStart) == interpolationPrefix.char) quoteStart++
            interpolated = quoteStart > start
        }
        var quoteEnd = quoteStart
        while (context.code.getOrNull(quoteEnd) == quote) quoteEnd++
        val quoteCount = quoteEnd - quoteStart
        if (quoteCount < minimumQuoteCount) return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = quoteEnd,
                closing = FixedStringLiteralClosingRule(quote.toString().repeat(quoteCount)),
                escapes = EscapeMode.None,
                interpolation = if (interpolated) interpolation else emptyList(),
                role = role,
            ),
        )
    }
}

internal data class SlashDelimitedStringRule(
    val interpolation: List<InterpolationRule> = emptyList(),
    val expressionStartCharacters: Set<Char?> = setOf(null, '=', '(', '[', '{', ',', ':'),
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf('/')

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (context.code.getOrNull(start) != '/' || context.code.getOrNull(start + 1) in setOf('/', '*')) return null
        if (context.previousNonWhitespace(start) !in expressionStartCharacters) return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = start + 1,
                closing = FixedStringLiteralClosingRule("/"),
                escapes = EscapeMode.Backslash,
                interpolation = interpolation,
                role = role,
            ),
        )
    }
}

internal data class DollarSlashDelimitedStringRule(
    val interpolation: List<InterpolationRule> = emptyList(),
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf('$')

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (!context.startsWith(start, "$/")) return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = start + 2,
                closing = FixedStringLiteralClosingRule("/$"),
                escapes = EscapeMode.None,
                interpolation = interpolation,
                role = role,
            ),
        )
    }
}
