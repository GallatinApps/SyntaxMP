package com.gallatinapps.syntaxmp.primitives.strings

import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.isIdentifierPart

internal interface StringLiteralStartRule {
    val leadingChars: Set<Char>
    fun tryMatch(context: StringLiteralScope, start: Int): Int?
}

internal fun interface StringLiteralBoundaryRule {
    val leadingChars: Set<Char>?
        get() = null

    fun tryMatch(context: StringLiteralScope, index: Int, contentEnd: Int): Int?
}

internal data class QuotedStringRule(
    val quotes: Set<Char>,
    val escapes: EscapeMode,
    val interpolation: Map<Char, List<InterpolationRule>> = emptyMap(),
    val formats: Map<Char, List<FormatSpecifierRule>> = emptyMap(),
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = quotes

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        val quote = context.code.getOrNull(start)?.takeIf { it in quotes } ?: return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = start + 1,
                closing = FixedStringLiteralClosingRule(quote.toString()),
                escapes = escapes,
                interpolation = interpolation[quote].orEmpty(),
                formats = formats[quote].orEmpty(),
                role = role,
            ),
        )
    }
}

internal data class TripleQuotedStringRule(
    val quote: Char,
    val escapes: EscapeMode,
    val interpolation: List<InterpolationRule> = emptyList(),
    val formats: List<FormatSpecifierRule> = emptyList(),
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf(quote)
    private val delimiter = quote.toString().repeat(3)

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (!context.startsWith(start, delimiter)) return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = start + delimiter.length,
                closing = FixedStringLiteralClosingRule(delimiter),
                escapes = escapes,
                interpolation = interpolation,
                formats = formats,
                role = role,
            ),
        )
    }
}

internal data class PrefixedQuotedStringRule(
    val prefixes: StringPrefixRule,
    val quotes: Set<Char>,
    val allowTripleQuote: Boolean = false,
    val escapeModeForPrefix: (String) -> EscapeMode,
    val tripleEscapeModeForPrefix: (String) -> EscapeMode = { EscapeMode.None },
    val interpolationRulesForPrefix: (String) -> List<InterpolationRule> = { emptyList() },
    val formats: List<FormatSpecifierRule> = emptyList(),
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = prefixes.leadingChars

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (start > 0 && context.code[start - 1].isIdentifierPart()) return null
        val prefix = prefixes.tryMatch(context.code, start) ?: return null
        val quote = context.code.getOrNull(prefix.endExclusive)?.takeIf { it in quotes } ?: return null
        val delimiterLength = if (
            allowTripleQuote &&
            context.startsWith(prefix.endExclusive, quote.toString().repeat(3))
        ) {
            3
        } else {
            1
        }
        val delimiter = quote.toString().repeat(delimiterLength)
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = prefix.endExclusive + delimiterLength,
                closing = FixedStringLiteralClosingRule(delimiter),
                escapes = if (delimiterLength == 3) {
                    tripleEscapeModeForPrefix(prefix.text)
                } else {
                    escapeModeForPrefix(prefix.text)
                },
                interpolation = interpolationRulesForPrefix(prefix.text),
                formats = formats,
                role = role,
            ),
        )
    }
}

internal data class AdjacentPrefixQuotedStringRule(
    val prefix: String,
    val quote: Char,
    val escapes: EscapeMode,
    val interpolation: List<InterpolationRule> = emptyList(),
    val formats: List<FormatSpecifierRule> = emptyList(),
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf(quote)

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        val prefixStart = start - prefix.length
        if (context.code.getOrNull(start) != quote || prefixStart < 0) return null
        if (!context.code.regionMatches(prefixStart, prefix, 0, prefix.length)) return null
        if (prefixStart > 0 && context.code[prefixStart - 1].isIdentifierPart()) return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = start + 1,
                closing = FixedStringLiteralClosingRule(quote.toString()),
                escapes = escapes,
                interpolation = interpolation,
                formats = formats,
                role = role,
            ),
        )
    }
}

internal data class BacktickIdentifierRule(
    val role: SyntaxRole = SyntaxRole.String,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf('`')

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (context.code.getOrNull(start) != '`') return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = start + 1,
                closing = FixedStringLiteralClosingRule("`"),
                escapes = EscapeMode.None,
                role = role,
            ),
        )
    }
}
