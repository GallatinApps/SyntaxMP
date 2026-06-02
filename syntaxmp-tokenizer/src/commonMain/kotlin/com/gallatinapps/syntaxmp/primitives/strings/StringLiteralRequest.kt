package com.gallatinapps.syntaxmp.primitives.strings

import com.gallatinapps.syntaxmp.role.SyntaxRole

internal data class StringLiteralRequest(
    val tokenStart: Int,
    val contentStart: Int,
    val contentEnd: Int? = null,
    val literalEnd: Int? = null,
    val closing: StringLiteralClosingRule,
    val escapes: EscapeMode,
    val interpolation: List<InterpolationRule> = emptyList(),
    val formats: List<FormatSpecifierRule> = emptyList(),
    val doubledQuoteEscapes: Boolean = false,
    val role: SyntaxRole = SyntaxRole.String,
)

internal data class ClosingResult(
    val contentEnd: Int,
    val literalEnd: Int,
)

internal sealed interface StringLiteralClosingRule {
    val leadingChars: Set<Char>?
        get() = null

    fun tryMatch(code: String, index: Int, hardEnd: Int): ClosingResult?
}

internal data class FixedStringLiteralClosingRule(
    val text: String,
) : StringLiteralClosingRule {
    override val leadingChars: Set<Char>? = text.firstOrNull()?.let(::setOf)

    override fun tryMatch(code: String, index: Int, hardEnd: Int): ClosingResult? =
        if (
            text.isNotEmpty() &&
            index + text.length <= hardEnd &&
            code.regionMatches(index, text, 0, text.length)
        ) {
            ClosingResult(contentEnd = index, literalEnd = index + text.length)
        } else {
            null
        }
}

internal data class LineAnchoredStringLiteralClosingRule(
    val text: String,
    val allowIndented: Boolean = false,
    val allowSemicolonTerminator: Boolean = false,
) : StringLiteralClosingRule {
    override fun tryMatch(code: String, index: Int, hardEnd: Int): ClosingResult? {
        if (index > 0 && code[index - 1] != '\n') return null
        val lineEnd = code.indexOf('\n', index).let { if (it == -1) hardEnd else it.coerceAtMost(hardEnd) }
        var cursor = index
        if (allowIndented) {
            while (cursor < lineEnd && code[cursor] in " \t") cursor++
        }
        if (!code.regionMatches(cursor, text, 0, text.length)) return null
        var after = cursor + text.length
        if (allowSemicolonTerminator && code.getOrNull(after) == ';') after++
        while (after < lineEnd && code[after] in " \t") after++
        return if (after == lineEnd) {
            ClosingResult(contentEnd = index, literalEnd = lineEnd)
        } else {
            null
        }
    }
}

internal object NoStringLiteralClosingRule : StringLiteralClosingRule {
    override val leadingChars: Set<Char> = emptySet()

    override fun tryMatch(code: String, index: Int, hardEnd: Int): ClosingResult? = null
}

internal enum class EscapeMode {
    None,
    Backslash,
}
