package com.gallatinapps.syntaxmp.languages.ruby

import com.gallatinapps.syntaxmp.engine.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.engine.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.engine.scanners.script.ScriptIdentifierOptions
import com.gallatinapps.syntaxmp.engine.scanners.script.ScriptLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.script.ScriptLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.HeredocRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.HeredocMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentLiteralRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan

internal object RubyTokenizer : LanguageTokenizer {
    private val hashBraceInterpolation = listOf(
        BalancedInterpolationRule(opener = "#{", openBrace = '{', closeBrace = '}'),
    )
    private val scannerOptions = ScriptLikeScannerOptions(
        identifiers = ScriptIdentifierOptions(
            sigilVariablePrefixes = setOf('@', '$'),
            suffixes = setOf('?', '!'),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                HeredocRule(
                    operator = "<<",
                    modes = setOf(HeredocMode.Plain, HeredocMode.Indented, HeredocMode.TrimIndented),
                    delimiterQuotes = setOf(null, '\'', '"', '`'),
                    bodyInterpolation = { delimiter ->
                        if (delimiter.quote == '\'') emptyList() else hashBraceInterpolation
                    },
                ),
                PercentLiteralRule(
                    marker = '%',
                    specifiers = mapOf(
                        'q' to PercentLiteralOptions(interpolation = emptyList()),
                        'Q' to PercentLiteralOptions(interpolation = hashBraceInterpolation),
                        'r' to PercentLiteralOptions(interpolation = hashBraceInterpolation),
                        'w' to PercentLiteralOptions(interpolation = emptyList()),
                        'W' to PercentLiteralOptions(interpolation = hashBraceInterpolation),
                        'i' to PercentLiteralOptions(interpolation = emptyList()),
                        'I' to PercentLiteralOptions(interpolation = hashBraceInterpolation),
                    ),
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    interpolation = mapOf('"' to hashBraceInterpolation),
                ),
            ),
        ),
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        ScriptLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = RubyKeywordRoles,
            constants = RubyConstants,
            builtinRoles = RubyBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
