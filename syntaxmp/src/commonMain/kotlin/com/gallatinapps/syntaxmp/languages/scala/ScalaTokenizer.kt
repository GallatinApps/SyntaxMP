package com.gallatinapps.syntaxmp.languages.scala

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BacktickIdentifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.DollarIdentifierInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.ExactStringPrefixes
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralScope
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralStartRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.PercentFormatSpecifierRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.PrefixedQuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.TripleQuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierPart
import com.gallatinapps.syntaxmp.engine.primitives.isIdentifierStart

internal object ScalaTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        typeSuffixes = setOf("L", "l", "F", "f", "D", "d"),
    )
    private val dollarInterpolation = listOf(
        DollarIdentifierInterpolationRule,
        BalancedInterpolationRule(opener = "${'$'}{", openBrace = '{', closeBrace = '}'),
    )
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions.AtSign,
        qualifiedNames = QualifiedNameOptions.after(
            "export",
            "import",
            "package",
            wildcardMarkers = setOf("*", "_"),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                PrefixedQuotedStringRule(
                    prefixes = ExactStringPrefixes(setOf("raw")),
                    quotes = setOf('"'),
                    allowTripleQuote = true,
                    escapeModeForPrefix = { EscapeMode.None },
                    interpolationRulesForPrefix = { dollarInterpolation },
                ),
                PrefixedQuotedStringRule(
                    prefixes = ExactStringPrefixes(setOf("f")),
                    quotes = setOf('"'),
                    allowTripleQuote = true,
                    escapeModeForPrefix = { EscapeMode.Backslash },
                    interpolationRulesForPrefix = { dollarInterpolation },
                    formats = listOf(PercentFormatSpecifierRule),
                ),
                PrefixedQuotedStringRule(
                    prefixes = ExactStringPrefixes(setOf("s")),
                    quotes = setOf('"'),
                    allowTripleQuote = true,
                    escapeModeForPrefix = { EscapeMode.Backslash },
                    interpolationRulesForPrefix = { dollarInterpolation },
                ),
                TripleQuotedStringRule(
                    quote = '"',
                    escapes = EscapeMode.None,
                ),
                ScalaSymbolLiteralRule,
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                ),
                BacktickIdentifierRule(role = SyntaxRole.Variable),
            ),
        ),
        numbers = numberScanner,
    )

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = ScalaKeywordRoles,
                constants = ScalaConstants,
                typeKeywords = ScalaTypeKeywords,
                builtinRoles = ScalaBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}

private object ScalaSymbolLiteralRule : StringLiteralStartRule {
    override val leadingChars: Set<Char> = setOf('\'')

    override fun tryMatch(context: StringLiteralScope, start: Int): Int? {
        if (context.code.getOrNull(start) != '\'') return null
        if (context.code.getOrNull(start + 1)?.isIdentifierStart() == true) {
            var end = start + 2
            while (end < context.code.length && context.code[end].isIdentifierPart()) end++
            if (context.code.getOrNull(end) == '\'') return null
            context.emit(start, end, SyntaxRole.Constant)
            return end
        }
        if (context.code.getOrNull(start + 1) in ScalaSymbolOperatorChars) {
            var end = start + 2
            while (context.code.getOrNull(end) in ScalaSymbolOperatorChars) end++
            if (context.code.getOrNull(end) == '\'') return null
            context.emit(start, end, SyntaxRole.Constant)
            return end
        }
        return null
    }
}

private val ScalaSymbolOperatorChars = setOf(
    '+', '-', '*', '/', '<', '>', '=', '!', '?', ':', '|', '&', '^', '~', '%',
)
