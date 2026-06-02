package com.gallatinapps.syntaxmp.builtins.dart

import com.gallatinapps.syntaxmp.tokenizer.LanguageTokenizer
import com.gallatinapps.syntaxmp.tokenizer.TokenizeRequest
import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.primitives.strings.DollarIdentifierInterpolationRule
import com.gallatinapps.syntaxmp.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.primitives.strings.ExactStringPrefixes
import com.gallatinapps.syntaxmp.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.primitives.strings.PrefixedQuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.primitives.strings.TripleQuotedStringRule
import com.gallatinapps.syntaxmp.spans.SyntaxTokenSpan

internal object DartTokenizer : LanguageTokenizer {
    private val numberScanner = ConfigurableNumberScanner(
        allowHex = true,
        typeSuffixes = emptySet(),
    )
    private val dollarInterpolation = listOf(
        DollarIdentifierInterpolationRule,
        BalancedInterpolationRule(opener = "${'$'}{", openBrace = '{', closeBrace = '}'),
    )
    private val scannerOptions = CLikeScannerOptions(
        annotations = AnnotationOptions.AtSign,
        identifiers = IdentifierOptions(
            classifyUppercaseCallsAsTypes = true,
            enumMemberRole = SyntaxRole.Constant,
            constantDeclarationKeywords = setOf("const"),
            constantDeclarationRequiresAssignment = true,
        ),
        qualifiedNames = QualifiedNameOptions(
            introducers = mapOf(
                "library" to emptySet(),
                "part" to setOf("of"),
            ),
        ),
        literals = StringLiteralOptions(
            startRules = listOf(
                PrefixedQuotedStringRule(
                    prefixes = ExactStringPrefixes(setOf("r", "R")),
                    quotes = setOf('"', '\''),
                    allowTripleQuote = true,
                    escapeModeForPrefix = { EscapeMode.None },
                ),
                TripleQuotedStringRule(
                    quote = '"',
                    escapes = EscapeMode.None,
                    interpolation = dollarInterpolation,
                ),
                TripleQuotedStringRule(
                    quote = '\'',
                    escapes = EscapeMode.None,
                    interpolation = dollarInterpolation,
                ),
                QuotedStringRule(
                    quotes = setOf('"', '\''),
                    escapes = EscapeMode.Backslash,
                    interpolation = mapOf('"' to dollarInterpolation, '\'' to dollarInterpolation),
                ),
            ),
        ),
        numbers = numberScanner,
    )

    override fun tokenize(request: TokenizeRequest): List<SyntaxTokenSpan> =
        CLikeScanner(
            code = request.code,
            language = request.languageId,
            keywordRoles = DartKeywordRoles,
            constants = DartConstants,
            typeKeywords = DartTypeKeywords,
            builtinRoles = DartBuiltinRoles,
            options = scannerOptions,
        ).scan()
}
