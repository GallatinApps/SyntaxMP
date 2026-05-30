package com.gallatinapps.syntaxmp.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import com.gallatinapps.syntaxmp.compose.theme.SyntaxTheme
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizerEngine

/**
 * Builds an [AnnotatedString] by applying syntax styles to [code].
 *
 * Use this for read-only renders into Compose text composables like
 * `BasicText`. For editable text (`BasicTextField` `outputTransformation`),
 * use [buildSyntaxStyledSpans] and [applySyntaxStyledSpans] instead; the
 * `TextFieldBuffer` path requires spans split at line breaks, which this
 * function does not do.
 */
public fun buildSyntaxAnnotatedString(
    code: String,
    spans: List<SyntaxTokenSpan>,
    theme: SyntaxTheme,
): AnnotatedString {
    val builder = AnnotatedString.Builder(code)
    spans.forEach { span ->
        val safeStart = span.start.coerceIn(0, code.length)
        val safeEnd = span.endExclusive.coerceIn(safeStart, code.length)
        if (safeEnd > safeStart) {
            builder.addStyle(
                style = theme.resolveSpanStyle(span),
                start = safeStart,
                end = safeEnd,
            )
        }
    }
    return builder.toAnnotatedString()
}

/**
 * Composable helper that produces the [AnnotatedString] for [code] with the
 * correct `remember` keys for each stage of the pipeline:
 *
 * - the tokenized spans are remembered on `(engine, code, languageLabel)`
 * - the resulting [AnnotatedString] is remembered on `(code, spans, theme)`
 *
 * Use this instead of writing the `remember` chain by hand. Getting one of
 * the keys wrong silently breaks live theme changes or causes redundant
 * tokenization.
 *
 * If [languageLabel] is `null` or blank, returns an unstyled
 * [AnnotatedString] of [code] without invoking [engine]. This matches the
 * intent of "no language picked yet."
 */
@Composable
public fun rememberSyntaxAnnotatedString(
    code: String,
    languageLabel: String?,
    engine: SyntaxTokenizerEngine,
    theme: SyntaxTheme,
): AnnotatedString {
    if (languageLabel.isNullOrBlank()) {
        return remember(code) { AnnotatedString(code) }
    }
    val spans = remember(engine, code, languageLabel) {
        engine.tokenize(code = code, languageLabel = languageLabel)
    }
    return remember(code, spans, theme) {
        buildSyntaxAnnotatedString(
            code = code,
            spans = spans,
            theme = theme,
        )
    }
}
