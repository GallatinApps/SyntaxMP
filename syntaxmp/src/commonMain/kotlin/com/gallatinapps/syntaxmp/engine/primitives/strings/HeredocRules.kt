package com.gallatinapps.syntaxmp.engine.primitives.strings

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierPart

internal enum class HeredocMode {
    Plain,
    Indented,
    TrimIndented,
}

internal data class HeredocMarker(
    val text: String,
    val quote: Char?,
)

internal data class HeredocRule(
    val operator: String,
    val modes: Set<HeredocMode> = setOf(HeredocMode.Plain),
    val delimiterQuotes: Set<Char?> = setOf(null),
    val allowSemicolonTerminator: Boolean = false,
    val bodyInterpolation: (HeredocMarker) -> List<InterpolationRule>,
) : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf(operator.first())

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (!context.startsWith(start, operator) || !canStartOperatorLikeLiteral(context, start)) return null
        val parsed = parseHeader(context, start) ?: return null
        return context.scanStringLiteral(
            StringLiteralRequest(
                tokenStart = start,
                contentStart = parsed.bodyStart,
                closing = LineAnchoredStringLiteralClosingRule(
                    text = parsed.delimiter.text,
                    allowIndented = parsed.mode != HeredocMode.Plain,
                    allowSemicolonTerminator = allowSemicolonTerminator,
                ),
                escapes = EscapeMode.None,
                interpolation = bodyInterpolation(parsed.delimiter),
                role = SyntaxRole.String,
            ),
        )
    }

    private fun parseHeader(context: StringLiteralScope, start: Int): HeredocHeaderResult? {
        var cursor = start + operator.length
        val mode = when (context.code.getOrNull(cursor)) {
            '-' -> HeredocMode.Indented.takeIf { it in modes }?.also { cursor++ }
            '~' -> HeredocMode.TrimIndented.takeIf { it in modes }?.also { cursor++ }
            else -> HeredocMode.Plain.takeIf { it in modes }
        } ?: return null

        while (cursor < context.code.length && context.code[cursor] in " \t") cursor++
        val quote = context.code.getOrNull(cursor)?.takeIf { it == '\'' || it == '"' || it == '`' }
        if (quote != null) {
            if (quote !in delimiterQuotes) return null
            cursor++
        } else if (null !in delimiterQuotes) {
            return null
        }
        val delimiterStart = cursor
        if (quote != null) {
            while (cursor < context.code.length && context.code[cursor] != quote && context.code[cursor] != '\n') {
                cursor++
            }
            if (cursor >= context.code.length || context.code[cursor] != quote) return null
        } else {
            while (cursor < context.code.length && context.code[cursor].isIdentifierPart()) cursor++
        }
        if (cursor == delimiterStart) return null
        val delimiter = context.code.substring(delimiterStart, cursor)
        val lineEnd = lineEnd(context.code, cursor)
        return HeredocHeaderResult(
            delimiter = HeredocMarker(text = delimiter, quote = quote),
            mode = mode,
            bodyStart = if (lineEnd < context.code.length) lineEnd + 1 else lineEnd,
        )
    }
}

private data class HeredocHeaderResult(
    val delimiter: HeredocMarker,
    val mode: HeredocMode,
    val bodyStart: Int,
)

private fun lineEnd(code: String, start: Int): Int =
    code.indexOf('\n', start).let { if (it == -1) code.length else it }
