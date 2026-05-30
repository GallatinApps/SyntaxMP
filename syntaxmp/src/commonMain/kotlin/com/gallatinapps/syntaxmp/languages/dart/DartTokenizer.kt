package com.gallatinapps.syntaxmp.languages.dart

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.engine.primitives.numbers.ConfigurableNumberScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.AnnotationOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScanner
import com.gallatinapps.syntaxmp.engine.scanners.clike.CLikeScannerOptions
import com.gallatinapps.syntaxmp.engine.scanners.clike.IdentifierOptions
import com.gallatinapps.syntaxmp.engine.primitives.QualifiedNameOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.BalancedInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.DollarIdentifierInterpolationRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.EscapeMode
import com.gallatinapps.syntaxmp.engine.primitives.strings.ExactStringPrefixes
import com.gallatinapps.syntaxmp.engine.primitives.strings.StringLiteralOptions
import com.gallatinapps.syntaxmp.engine.primitives.strings.PrefixedQuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.QuotedStringRule
import com.gallatinapps.syntaxmp.engine.primitives.strings.TripleQuotedStringRule

internal object DartTokenizer {
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

    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            CLikeScanner(
                code = request.code,
                language = request.languageId,
                keywordRoles = DartKeywordRoles,
                constants = DartConstants,
                typeKeywords = DartTypeKeywords,
                builtinRoles = DartBuiltinRoles,
                options = scannerOptions,
            ).scan(),
        )
}
