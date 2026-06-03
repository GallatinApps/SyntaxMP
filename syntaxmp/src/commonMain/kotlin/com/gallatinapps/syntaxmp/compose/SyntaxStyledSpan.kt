package com.gallatinapps.syntaxmp.compose

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.SpanStyle
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

/**
 * A token span with its theme-resolved [SpanStyle].
 *
 * Distinct from [SyntaxTokenSpan]: a `SyntaxTokenSpan` is the raw engine
 * output and carries a `SyntaxRole` and `LanguageId`. It is
 * theme-independent and cacheable. A `SyntaxStyledSpan` is that span after a
 * [SyntaxTheme] has resolved its role to a concrete Compose [SpanStyle],
 * ready to apply to text.
 *
 * @property start Inclusive start offset in the target text.
 * @property endExclusive Exclusive end offset in the target text.
 * @property style Compose style applied over this range.
 */
public data class SyntaxStyledSpan(
    val start: Int,
    val endExclusive: Int,
    val style: SpanStyle,
)

/**
 * Converts engine token spans into theme-resolved styled spans, splitting
 * runs at line breaks so no produced span straddles a `\n` or `\r\n`.
 *
 * Use this when the downstream consumer is `TextFieldBuffer`-shaped: either
 * directly via [applySyntaxStyledSpans] inside a `BasicTextField`
 * `outputTransformation`, or via an intermediate styling pipeline.
 *
 * The line split is required for the editable path because
 * [TextFieldBuffer.addStyle] misbehaves on ranges that straddle line breaks.
 * For read-only [BasicText] surfaces, use [buildSyntaxAnnotatedString]
 * instead. `AnnotatedString` handles multi-line ranges correctly, and
 * skipping the split saves an allocation.
 */
public fun buildSyntaxStyledSpans(
    code: String,
    spans: List<SyntaxTokenSpan>,
    theme: SyntaxTheme,
): List<SyntaxStyledSpan> = buildList {
    spans.forEach { span ->
        addStyledSpans(
            code = code,
            start = span.start,
            endExclusive = span.endExclusive,
            style = theme.resolveSpanStyle(span),
        )
    }
}

private fun MutableList<SyntaxStyledSpan>.addStyledSpans(
    code: String,
    start: Int,
    endExclusive: Int,
    style: SpanStyle,
) {
    val safeStart = start.coerceIn(0, code.length)
    val safeEnd = endExclusive.coerceIn(safeStart, code.length)
    if (safeEnd <= safeStart) {
        return
    }

    var segmentStart = safeStart
    var index = safeStart
    while (index < safeEnd) {
        val char = code[index]
        if (char == '\n' || char == '\r') {
            if (segmentStart < index) {
                add(SyntaxStyledSpan(segmentStart, index, style))
            }
            index = if (char == '\r' && index + 1 < safeEnd && code[index + 1] == '\n') {
                index + 2
            } else {
                index + 1
            }
            segmentStart = index
        } else {
            index++
        }
    }
    if (segmentStart < safeEnd) {
        add(SyntaxStyledSpan(segmentStart, safeEnd, style))
    }
}

/**
 * Coerces styled spans to the valid range of a mutable text buffer.
 *
 * Used by [applySyntaxStyledSpans] to guard against the race between a
 * keystroke and the tokenization snapshot it was produced from: the buffer
 * may be a character shorter than the code the spans index into. Spans that
 * fall outside `[0, textLength]` are clipped; spans that collapse to zero
 * width after clipping are dropped.
 */
internal fun syntaxStyledBufferSpans(
    textLength: Int,
    spans: List<SyntaxStyledSpan>,
): List<SyntaxStyledSpan> {
    val safeTextLength = textLength.coerceAtLeast(0)
    return buildList {
        spans.forEach { span ->
            val safeStart = span.start.coerceIn(0, safeTextLength)
            val safeEnd = span.endExclusive.coerceIn(safeStart, safeTextLength)
            if (safeEnd > safeStart) {
                add(
                    span.copy(
                        start = safeStart,
                        endExclusive = safeEnd,
                    ),
                )
            }
        }
    }
}

/**
 * Applies styled spans to this [TextFieldBuffer] from inside a
 * `BasicTextField` `outputTransformation` block.
 *
 * Each span is clipped to the buffer's current length before being applied.
 * This handles the race between a keystroke and the tokenization snapshot the
 * spans were produced from: the buffer may be a character shorter than the
 * code the spans index into. Spans that fall entirely outside the buffer are
 * silently dropped.
 */
public fun TextFieldBuffer.applySyntaxStyledSpans(
    spans: List<SyntaxStyledSpan>,
) {
    syntaxStyledBufferSpans(
        textLength = length,
        spans = spans,
    ).forEach { span ->
        addStyle(
            spanStyle = span.style,
            start = span.start,
            end = span.endExclusive,
        )
    }
}
